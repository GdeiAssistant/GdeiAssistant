package cn.gdeiassistant.core.social.controller;
import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.core.social.pojo.dto.*;
import cn.gdeiassistant.core.social.service.*;
import cn.gdeiassistant.core.social.exception.SocialException;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;
import java.io.ByteArrayInputStream;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class SocialHttpContractTest {
    private final SocialRelationService relations=mock(SocialRelationService.class);
    private final SocialChatService chat=mock(SocialChatService.class);
    private final SocialIdentityService identity=mock(SocialIdentityService.class);
    private final R2StorageService storage=mock(R2StorageService.class);
    private MockMvc http;
    @BeforeEach void setup(){var controller=new SocialController();ReflectionTestUtils.setField(controller,"relationService",relations);ReflectionTestUtils.setField(controller,"chatService",chat);ReflectionTestUtils.setField(controller,"identityService",identity);ReflectionTestUtils.setField(controller,"r2StorageService",storage);http=MockMvcBuilders.standaloneSetup(controller).build();}
    @Test void userReadsAndRelationshipWritesPreservePaginationAndPublicIdentity() throws Exception {
        var peer=new SocialUserDTO();peer.setId("synthetic-public-id");peer.setNickname("合成昵称");var page=new PageDTO<>(List.of(peer),"synthetic-cursor",true);
        when(relations.getMe("synthetic-session")).thenReturn(peer);when(relations.searchUsers("synthetic-session","name","next",10)).thenReturn(page);
        when(relations.getUser("synthetic-session","peer")).thenReturn(peer);when(relations.follow("synthetic-session","peer")).thenReturn(peer);when(relations.unfollow("synthetic-session","peer")).thenReturn(peer);
        when(relations.listRelationships("synthetic-session","peer","FOLLOWERS","next",10)).thenReturn(page);when(relations.listBlocks("synthetic-session","next",10)).thenReturn(page);
        http.perform(get("/api/social/me").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.data.id").value("synthetic-public-id"));
        http.perform(get("/api/social/users").param("query","name").param("cursor","next").param("limit","10").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.data.hasMore").value(true)).andExpect(jsonPath("$.data.nextCursor").value("synthetic-cursor"));
        http.perform(get("/api/social/users/peer").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.data.nickname").value("合成昵称"));
        http.perform(get("/api/social/users/peer/relationships").param("kind","FOLLOWERS").param("cursor","next").param("limit","10").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.data.items[0].id").value("synthetic-public-id"));
        http.perform(get("/api/social/blocks").param("cursor","next").param("limit","10").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.data.items[0].id").value("synthetic-public-id"));
        http.perform(put("/api/social/users/peer/follow").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(true));
        http.perform(delete("/api/social/users/peer/follow").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(true));
        http.perform(put("/api/social/users/peer/block").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(true));verify(relations).block("synthetic-session","peer");
        http.perform(delete("/api/social/users/peer/block").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(true));verify(relations).unblock("synthetic-session","peer");
        when(relations.getPrivacy("synthetic-session")).thenReturn(Map.of("dmPolicy","MUTUAL"));
        http.perform(get("/api/social/privacy").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.data.dmPolicy").value("MUTUAL"));
        http.perform(put("/api/social/privacy").requestAttr("sessionId","synthetic-session").contentType("application/json").content("{\"dmPolicy\":\"NONE\"}")).andExpect(jsonPath("$.success").value(true));verify(relations).updatePrivacy("synthetic-session","NONE");
    }
    @Test void conversationRoutesKeepSequenceAndClientIdSemantics() throws Exception {
        var message=new ChatMessageDTO();message.setId("20");message.setSeq("3");message.setClientMessageId("synthetic-client");message.setContent("合成消息");
        when(chat.listMessages("synthetic-session","10","5",null,10)).thenReturn(new PageDTO<>(List.of(message),"next",true));
        when(chat.sendMessage("synthetic-session","10","synthetic-client","合成消息")).thenReturn(message);
        http.perform(get("/api/social/conversations/10/messages").requestAttr("sessionId","synthetic-session").param("beforeSeq","5").param("limit","10")).andExpect(jsonPath("$.data.items[0].seq").value("3"));
        http.perform(post("/api/social/conversations/10/messages").requestAttr("sessionId","synthetic-session").contentType("application/json").content("{\"clientMessageId\":\"synthetic-client\",\"content\":\"合成消息\"}")).andExpect(jsonPath("$.data.clientMessageId").value("synthetic-client"));
        http.perform(put("/api/social/conversations/10/read").requestAttr("sessionId","synthetic-session").contentType("application/json").content("{\"lastReadSeq\":\"3\"}")).andExpect(jsonPath("$.success").value(true));verify(chat).markRead("synthetic-session","10","3");
        http.perform(post("/api/social/conversations").requestAttr("sessionId","synthetic-session").contentType("application/json").content("{\"peerId\":\"peer\"}")).andExpect(jsonPath("$.success").value(true));verify(chat).createOrGetConversation("synthetic-session","peer");
        http.perform(get("/api/social/conversations").requestAttr("sessionId","synthetic-session").param("cursor","next").param("limit","10")).andExpect(jsonPath("$.success").value(true));verify(chat).listConversations("synthetic-session","next",10);
        http.perform(get("/api/social/conversations/10").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(true));verify(chat).getConversation("synthetic-session","10");
        http.perform(get("/api/social/unread").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(true));verify(chat).unreadTotal("synthetic-session");
    }
    @Test void errorsHaveCorrectHttpStatusAndImagesUsePrivateCachePolicy() throws Exception {
        when(relations.getMe(null)).thenThrow(SocialException.authRequired());http.perform(get("/api/social/me")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.errorCode").value("AUTH_REQUIRED"));
        http.perform(get("/api/social/users").param("limit","invalid")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
        var account=new CampusAccountView();account.setUsername("synthetic-owner");when(identity.requireActiveByPublicId("peer")).thenReturn(account);
        when(storage.downloadObject(null,"avatar/synthetic-owner.jpg")).thenReturn(new ByteArrayInputStream(new byte[]{1,2}));
        http.perform(get("/api/social/users/peer/avatar").requestAttr("sessionId","synthetic-session")).andExpect(status().isOk()).andExpect(header().string("Cache-Control","private, max-age=300")).andExpect(content().bytes(new byte[]{1,2}));
        when(storage.downloadObject(null,"avatar/synthetic-owner.jpg")).thenReturn(null);http.perform(get("/api/social/users/peer/avatar").requestAttr("sessionId","synthetic-session")).andExpect(status().isNoContent());
        account.setUsername(null);http.perform(get("/api/social/users/peer/avatar").requestAttr("sessionId","synthetic-session")).andExpect(status().isNotFound());
        http.perform(get("/api/social/conversations/10/messages/20/image").requestAttr("sessionId","synthetic-session")).andExpect(status().isNotFound());
        http.perform(multipart("/api/social/conversations/10/messages/image").file(new MockMultipartFile("image",new byte[]{})).param("clientMessageId","synthetic-client")).andExpect(status().isBadRequest());
        http.perform(multipart("/api/social/conversations/10/messages/image").file(new MockMultipartFile("image","fixture.png","image/png",new byte[]{1})).param("clientMessageId","synthetic-client").requestAttr("sessionId","synthetic-session")).andExpect(jsonPath("$.success").value(true));verify(chat).sendImageMessage(eq("synthetic-session"),eq("10"),eq("synthetic-client"),any(),eq("image/png"));
    }
}
