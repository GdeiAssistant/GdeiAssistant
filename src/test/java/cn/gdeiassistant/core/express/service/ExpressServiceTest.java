package cn.gdeiassistant.core.express.service;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.ExpressException.NoRealNameException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.express.mapper.ExpressMapper;
import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpressServiceTest {

    @Mock
    private ExpressMapper expressMapper;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private InteractionNotificationService interactionNotificationService;

    @InjectMocks
    private ExpressService expressService;

    @Test
    void addExpressCommentThrowsIllegalArgumentExceptionForEmptyComment() {
        assertThrows(IllegalArgumentException.class,
                () -> expressService.addExpressComment(1, "session1", ""));
    }

    @Test
    void addExpressCommentThrowsIllegalArgumentExceptionForCommentOver50Chars() {
        String longComment = "a".repeat(51);

        assertThrows(IllegalArgumentException.class,
                () -> expressService.addExpressComment(1, "session1", longComment));
    }

    @Test
    void addExpressCommentThrowsDataNotExistExceptionWhenExpressPostNotFound() {
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(new User("testuser"));
        when(expressMapper.selectExpressById(999, "testuser")).thenReturn(null);

        assertThrows(DataNotExistException.class,
                () -> expressService.addExpressComment(999, "session1", "nice post"));
    }

    @Test
    void addExpressCommentSucceedsWithValidComment() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        ExpressEntity entity = new ExpressEntity();
        entity.setId(1);
        entity.setUsername("poster");
        when(expressMapper.selectExpressById(1, "testuser")).thenReturn(entity);

        expressService.addExpressComment(1, "session1", "great confession!");

        verify(expressMapper).insertExpressComment(argThat(c ->
                c.getExpressId() == 1
                        && "testuser".equals(c.getUsername())
                        && "great confession!".equals(c.getComment())
        ));
        verify(interactionNotificationService).createInteractionNotification(
                eq("express"), eq("comment"), eq("poster"), eq("testuser"),
                eq("1"), any(), eq("comment"), anyString(), anyString()
        );
    }

    @Test
    void guessExpressReturnsTrueWhenNameMatchesRealname() throws Exception {
        User user = new User("guesser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        ExpressEntity entity = new ExpressEntity();
        entity.setId(1);
        entity.setUsername("poster");
        entity.setRealname("Alice");
        when(expressMapper.selectExpressById(1, "guesser")).thenReturn(entity);
        when(expressMapper.selectCorrectExpressGuessRecord("guesser")).thenReturn(0);

        boolean result = expressService.guessExpress(1, "session1", "Alice");

        assertTrue(result);
        verify(expressMapper).insertExpressGuess(1, "guesser", 1);
    }

    @Test
    void guessExpressThrowsNoRealNameExceptionWhenPostHasNoRealname() {
        User user = new User("guesser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        ExpressEntity entity = new ExpressEntity();
        entity.setId(1);
        entity.setUsername("poster");
        entity.setRealname(null);
        when(expressMapper.selectExpressById(1, "guesser")).thenReturn(entity);
        when(expressMapper.selectCorrectExpressGuessRecord("guesser")).thenReturn(0);

        assertThrows(NoRealNameException.class,
                () -> expressService.guessExpress(1, "session1", "Alice"));
    }
}
