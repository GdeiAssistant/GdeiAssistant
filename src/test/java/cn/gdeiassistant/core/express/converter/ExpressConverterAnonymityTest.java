package cn.gdeiassistant.core.express.converter;

import cn.gdeiassistant.common.pojo.Entity.ExpressComment;
import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import cn.gdeiassistant.core.express.pojo.vo.ExpressCommentVO;
import cn.gdeiassistant.core.express.pojo.vo.ExpressVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpressConverterAnonymityTest {

    private ExpressConverter expressConverter;
    private ExpressCommentConverter commentConverter;

    @BeforeEach
    void setUp() {
        expressConverter = new ExpressConverterImpl();
        commentConverter = new ExpressCommentConverterImpl();
    }

    @Test
    void toVOHidesCampusUsernameAndRealnameKeepsNicknameAndCanGuess() {
        ExpressEntity entity = new ExpressEntity();
        entity.setId(3);
        entity.setUsername("campus_alice");
        entity.setNickname("墙内昵称");
        entity.setRealname("真实名");
        entity.setContent("表白内容");
        entity.setPublishTime(new Date());

        ExpressVO vo = expressConverter.toVO(entity);

        assertNull(vo.getUsername());
        assertNull(vo.getRealname());
        assertEquals("墙内昵称", vo.getNickname());
        assertEquals("表白内容", vo.getContent());
        assertEquals(Boolean.TRUE, vo.getCanGuess());
        assertEquals("campus_alice", entity.getUsername());
        assertEquals("真实名", entity.getRealname());
    }

    @Test
    void commentVoHidesUsernameKeepsPublicNicknameWithoutMutatingEntity() {
        ExpressComment entity = new ExpressComment();
        entity.setId(8);
        entity.setExpressId(3);
        entity.setUsername("campus_bob");
        entity.setNickname("公开昵称");
        entity.setComment("加油");

        ExpressCommentVO vo = commentConverter.toVO(entity);
        assertEquals("公开昵称", vo.getNickname());
        assertEquals("加油", vo.getComment());
        assertEquals("campus_bob", entity.getUsername());
        // VO 类型本身不含 username 字段，序列化不会泄漏校园账号
        assertFalse(hasUsernameProperty(ExpressCommentVO.class));

        List<ExpressCommentVO> list = commentConverter.toVOList(List.of(entity));
        assertEquals(1, list.size());
        assertEquals("公开昵称", list.get(0).getNickname());
        assertEquals("campus_bob", entity.getUsername());
    }

    private static boolean hasUsernameProperty(Class<?> type) {
        try {
            type.getMethod("getUsername");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
