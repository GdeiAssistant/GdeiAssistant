package cn.gdeiassistant.core.social.service;

import cn.gdeiassistant.common.pojo.entity.Introduction;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.social.mapper.SocialRelationMapper;
import cn.gdeiassistant.core.social.mapper.SocialUserSummaryMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SocialIdentityPolicyTest {
    private final SocialIdentityService service=new SocialIdentityService();
    private final PrivacyMapper privacy=mock(PrivacyMapper.class);
    private final ProfileMapper profiles=mock(ProfileMapper.class);
    private final SocialRelationMapper relations=mock(SocialRelationMapper.class);
    private final SocialUserSummaryMapper summaries=mock(SocialUserSummaryMapper.class);
    private CampusAccountView user(long id){var user=new CampusAccountView();user.setId(id);user.setPublicId("synthetic-public-"+id);user.setUsername("synthetic-"+id);user.setStatus("ACTIVE");return user;}
    @BeforeEach void setup(){ReflectionTestUtils.setField(service,"privacyMapper",privacy);ReflectionTestUtils.setField(service,"profileMapper",profiles);ReflectionTestUtils.setField(service,"socialRelationMapper",relations);ReflectionTestUtils.setField(service,"summaries",summaries);}
    @ParameterizedTest
    @CsvSource({"ALL,false,false,true,NONE","NONE,true,true,false,MUTUAL","FOLLOWING,false,true,true,FOLLOWED_BY","FOLLOWING,true,false,false,FOLLOWING","MUTUAL,true,true,true,MUTUAL","MUTUAL,true,false,false,FOLLOWING","invalid,false,false,false,NONE"})
    void batchAndSinglePoliciesHaveTheSameDirectionAndDefaults(String policy,boolean following,boolean followedBy,boolean allowed,String relationship){
        var viewer=user(1);var target=user(2);var settings=new PrivacyEntity();settings.setDmPolicy(policy);when(privacy.selectPrivacy("synthetic-2")).thenReturn(settings);
        when(relations.countFollow(1L,2L)).thenReturn(following?1:0);when(relations.countFollow(2L,1L)).thenReturn(followedBy?1:0);
        var row=new HashMap<String,Object>();row.put("userId",2L);row.put("publicId",target.getPublicId());row.put("status","ACTIVE");row.put("policy",policy);
        row.put("nickname","合成昵称");row.put("introduction","公开简介");row.put("following",following?1:0);row.put("followedBy",followedBy);
        row.put("followingCount",3L);row.put("followerCount",4);row.put("friendCount",1L);
        when(summaries.selectUsers(1L,List.of(2L))).thenReturn(List.of(row));
        var batch=service.buildSocialUsers(viewer,List.of(2L,2L)).get(2L);var single=service.buildSocialUser(viewer,target);
        assertEquals(allowed,batch.isCanMessage());assertEquals(allowed,single.isCanMessage());assertEquals(relationship,batch.getRelationship());assertEquals(relationship,single.getRelationship());
        assertEquals(3,batch.getFollowingCount());assertEquals(4,batch.getFollowerCount());assertEquals(1,batch.getFriendCount());assertEquals(target.getPublicId(),batch.getId());
        if(!allowed)assertEquals("PRIVACY_RESTRICTED",batch.getMessagePermissionReason());
        verify(summaries).selectUsers(1L,List.of(2L));
    }
    @Test void introductionMissingPrivacyStaysPrivateButSelfCanReadAndClosedPeersExposeNoProfile(){
        var viewer=user(1);var target=user(2);var profile=new ProfileEntity();profile.setNickname("合成昵称");when(profiles.selectUserProfile("synthetic-2")).thenReturn(profile);
        var intro=new Introduction();intro.setIntroductionContent("合成私有简介");when(profiles.selectUserIntroduction("synthetic-2")).thenReturn(intro);
        assertNull(service.buildSocialUser(viewer,target).getIntroduction());verify(profiles,never()).selectUserIntroduction("synthetic-2");
        assertEquals("合成私有简介",service.buildSocialUser(target,target).getIntroduction());
        var settings=new PrivacyEntity();settings.setIntroductionOpen(true);settings.setDmPolicy(" all ");when(privacy.selectPrivacy("synthetic-2")).thenReturn(settings);
        assertEquals("合成私有简介",service.buildSocialUser(viewer,target).getIntroduction());assertEquals("ALL",service.normalizeDmPolicy(settings));
        var closed=service.buildClosedPeer(target);assertEquals(target.getPublicId(),closed.getId());assertNull(closed.getAvatarUrl());assertNull(closed.getIntroduction());assertFalse(closed.isCanMessage());
        assertEquals("",service.buildClosedPeer(null).getId());assertTrue(service.buildSocialUsers(viewer,List.of()).isEmpty());
    }
    @Test void selfBlockAndInactiveContactsDenyEvenAnOpenPolicy(){
        var viewer=user(1);var target=user(2);assertEquals("SELF",service.evaluateMessagePermission(viewer,viewer).reason);
        target.setStatus("CLOSED");assertEquals("CONTACT_UNAVAILABLE",service.evaluateMessagePermission(viewer,target).reason);target.setStatus("ACTIVE");
        when(relations.countAnyBlock(1L,2L)).thenReturn(1);assertEquals("CONTACT_UNAVAILABLE",service.evaluateMessagePermission(viewer,target).reason);
        var self=Map.<String,Object>of("userId",1L,"publicId",viewer.getPublicId(),"status","ACTIVE","policy","ALL","followingCount",0,"followerCount",0,"friendCount",0);
        var closed=Map.<String,Object>of("userId",2L,"publicId",target.getPublicId(),"status","CLOSED");
        when(summaries.selectUsers(1L,List.of(1L,2L))).thenReturn(List.of(self,closed));var result=service.buildSocialUsers(viewer,List.of(1L,2L));
        assertEquals("SELF",result.get(1L).getMessagePermissionReason());assertFalse(result.get(2L).isCanMessage());assertNull(result.get(2L).getAvatarUrl());
    }
    @Test void messageIdentityLookupDeduplicatesIdsAndKeepsClosedPublicIdentityOnly() {
        var closed=Map.<String,Object>of("userId",2L,"publicId","synthetic-closed");
        var missingPublicId=new HashMap<String,Object>();missingPublicId.put("userId",3L);
        when(summaries.selectPublicIds(List.of(2L,3L,4L))).thenReturn(List.of(closed,missingPublicId));
        assertEquals(Map.of(2L,"synthetic-closed"),service.findPublicIds(List.of(2L,2L,3L,4L)));
        verify(summaries).selectPublicIds(List.of(2L,3L,4L));
        assertTrue(service.findPublicIds(List.of()).isEmpty());verifyNoMoreInteractions(summaries);
    }
}
