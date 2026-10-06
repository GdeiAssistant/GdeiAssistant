package cn.gdeiassistant.core.secret.converter;

import cn.gdeiassistant.common.tools.Utils.AnonymizeUtils;
import cn.gdeiassistant.core.secret.pojo.entity.SecretCommentEntity;
import cn.gdeiassistant.core.secret.pojo.entity.SecretContentEntity;
import cn.gdeiassistant.core.secret.pojo.vo.SecretCommentVO;
import cn.gdeiassistant.core.secret.pojo.vo.SecretVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 真正跑 MapStruct 生成实现，验证公开 VO 匿名、实体字段不被映射覆盖。
 */
class SecretConverterAnonymityTest {

    private SecretConverter converter;
    private SecretCommentConverter commentConverter;

    @BeforeEach
    void setUp() {
        commentConverter = new SecretCommentConverterImpl();
        SecretConverterImpl impl = new SecretConverterImpl();
        ReflectionTestUtils.setField(impl, "secretCommentConverter", commentConverter);
        converter = impl;
    }

    @Test
    void toVOReplacesCampusUsernameWithAnonymousLabelWithoutMutatingEntity() {
        SecretContentEntity entity = new SecretContentEntity();
        entity.setId(7);
        entity.setUsername("campus_alice");
        entity.setContent("hello treehole");
        entity.setTheme(1);
        entity.setPublishTime(new Date());

        SecretVO vo = converter.toVO(entity);

        assertEquals(AnonymizeUtils.treeholeAnonymousLabel(), vo.getUsername());
        assertEquals("campus_alice", entity.getUsername());
        assertEquals("hello treehole", vo.getContent());
        assertEquals(7, vo.getId());
    }

    @Test
    void toVOListAndNestedCommentsStayAnonymous() {
        SecretCommentEntity comment = new SecretCommentEntity();
        comment.setId(3);
        comment.setContentId(7);
        comment.setUsername("campus_bob");
        comment.setComment("nice");
        comment.setAvatarTheme(2);

        SecretContentEntity entity = new SecretContentEntity();
        entity.setId(7);
        entity.setUsername("campus_alice");
        entity.setContent("body");
        entity.setSecretCommentList(List.of(comment));

        List<SecretVO> vos = converter.toVOList(List.of(entity));
        assertEquals(1, vos.size());
        assertEquals(AnonymizeUtils.treeholeAnonymousLabel(), vos.get(0).getUsername());

        SecretCommentVO commentVO = commentConverter.toVO(comment);
        assertEquals(AnonymizeUtils.treeholeAnonymousLabel(), commentVO.getUsername());
        assertEquals("campus_bob", comment.getUsername());
        assertEquals("nice", commentVO.getComment());
        assertEquals(2, commentVO.getAvatarTheme());

        if (vos.get(0).getSecretCommentList() != null && !vos.get(0).getSecretCommentList().isEmpty()) {
            assertEquals(AnonymizeUtils.treeholeAnonymousLabel(),
                    vos.get(0).getSecretCommentList().get(0).getUsername());
        }
    }
}
