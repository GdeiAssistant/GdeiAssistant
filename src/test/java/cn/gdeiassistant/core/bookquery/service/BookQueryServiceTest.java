package cn.gdeiassistant.core.bookquery.service;

import cn.gdeiassistant.common.exception.bookrenewexception.BookRenewOvertimeException;
import cn.gdeiassistant.common.exception.commonexception.*;
import cn.gdeiassistant.core.collectionquery.pojo.CollectionQueryResult;
import cn.gdeiassistant.core.collectionquery.service.CollectionQueryService;
import cn.gdeiassistant.core.userlogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.integration.library.LibraryClient;
import cn.gdeiassistant.integration.library.pojo.LibraryRenewResult;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookQueryServiceTest {
    private final LibraryClient client = mock(LibraryClient.class);
    private final CollectionQueryService collections = mock(CollectionQueryService.class);
    private final UserCertificateService certificates = mock(UserCertificateService.class);
    private final BookQueryService service = new BookQueryService();
    @BeforeEach void setup() throws Exception {
        ReflectionTestUtils.setField(service, "libraryClient", client);
        ReflectionTestUtils.setField(service, "collectionQueryService", collections);
        ReflectionTestUtils.setField(service, "userCertificateService", certificates);
        var certificate = new UserCertificateEntity(); certificate.setNumber("synthetic-card");
        when(certificates.getUserSessionCertificate("session")).thenReturn(certificate);
    }
    @Test void emptySearchIsExplicitAndExistingSearchIsPreserved() throws Exception {
        var empty = service.searchCollections("session", 1, "keyword");
        assertEquals(0, empty.getSumPage()); assertTrue(empty.getCollectionList().isEmpty());
        var found = new CollectionQueryResult(); when(collections.collectionQuery(2, "keyword")).thenReturn(found);
        assertSame(found, service.searchCollections("session", 2, "keyword"));
        service.getCollectionDetail("session", "synthetic-detail");
        verify(collections).getCollectionDetailByDetailURL("synthetic-detail");
    }
    @Test void borrowedBooksParseIndependentFieldsAndRenewalArguments() throws Exception {
        when(client.fetchBorrowedBooksPage("session", "synthetic-card", "synthetic-password")).thenReturn(Jsoup.parse("""
          <table class='tableLib'><tr><td>B001<a>remove</a></td><td>合成图书</td><td>合成作者</td>
          <td>2026-10-01</td><td>2026-11-01</td><td>1</td></tr><tr><td class='tableCon'>
          <a onclick="renew('serial','code', 'unused')">renew</a></td></tr></table>
          """));
        var books = service.getBorrowedBooks("session", "synthetic-password"); assertEquals(1, books.size());
        var book = books.get(0); assertEquals("B001", book.getId()); assertEquals("合成图书", book.getName());
        assertEquals("合成作者", book.getAuthor()); assertEquals("2026-10-01", book.getBorrowDate());
        assertEquals("2026-11-01", book.getReturnDate()); assertEquals(1, book.getRenewTime());
        assertEquals("serial", book.getSn()); assertEquals("code", book.getCode());
    }
    @Test void borrowFailuresKeepPasswordNetworkAndMalformedPageDistinct() throws Exception {
        when(client.fetchBorrowedBooksPage(anyString(), anyString(), anyString()))
                .thenThrow(new PasswordIncorrectException("synthetic"), new IOException("synthetic"))
                .thenReturn(Jsoup.parse("<table class='tableLib'></table>"));
        assertThrows(PasswordIncorrectException.class, () -> service.bookquery("session", "p"));
        assertThrows(NetWorkTimeoutException.class, () -> service.bookquery("session", "p"));
        assertThrows(ServerErrorException.class, () -> service.bookquery("session", "p"));
    }
    @Test void renewalSuccessLimitAndOutageAreNotInterchangeable() throws Exception {
        var ok = new LibraryRenewResult(); ok.setResult(1); ok.setMessage("ok");
        var limit = new LibraryRenewResult(); limit.setResult(1); limit.setMessage("超过最大续借次数:1！");
        when(client.renewBook("session", "serial", "code")).thenReturn(ok, limit, new LibraryRenewResult())
                .thenThrow(new IOException("synthetic outage"));
        service.renewBook("session", "serial", "code");
        assertThrows(BookRenewOvertimeException.class, () -> service.renewBook("session", "serial", "code"));
        assertThrows(ServerErrorException.class, () -> service.renewBook("session", "serial", "code"));
        assertThrows(NetWorkTimeoutException.class, () -> service.renewBook("session", "serial", "code"));
    }
}
