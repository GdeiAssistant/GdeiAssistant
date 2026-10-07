package cn.gdeiassistant.core.collectionquery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.gdeiassistant.common.exception.commonexception.*;
import cn.gdeiassistant.common.exception.queryexception.ErrorQueryConditionException;
import cn.gdeiassistant.integration.library.LibraryClient;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CollectionQueryServiceTest {
    @Mock LibraryClient library;
    @InjectMocks CollectionQueryService service;

    String item(String details) {
        return "<li><a href='detail?opacUrl=book&amp;search=keyword'></a><div class='title'><span>1"
                + " Book</span></div><div class='detail'>"
                + details
                + "</div></li>";
    }

    @Test
    void listParsesBooksAndOptionalAuthorAndPublisher() throws Exception {
        when(library.fetchCollectionListPage(1, "keyword"))
                .thenReturn(
                        Jsoup.parse(
                                "<select id='pagenum'><option>1/3</option></select><ul"
                                        + " class='list'>"
                                        + item("<p>Author</p><p>Publisher</p>")
                                        + item("<p>OnlyPublisher</p>")
                                        + item("")
                                        + "</ul>"));
        var r = service.collectionQuery(1, "keyword");
        assertEquals(3, r.getSumPage());
        assertEquals(3, r.getCollectionList().size());
        assertEquals("Book", r.getCollectionList().get(0).getBookname());
        assertEquals("Author", r.getCollectionList().get(0).getAuthor());
        assertEquals("Publisher", r.getCollectionList().get(0).getPublishingHouse());
        assertEquals("OnlyPublisher", r.getCollectionList().get(1).getPublishingHouse());
        assertNull(r.getCollectionList().get(2).getAuthor());
    }

    @Test
    void requestedPageBeyondTotalIsRejected() throws Exception {
        when(library.fetchCollectionListPage(4, "keyword"))
                .thenReturn(Jsoup.parse("<select id='pagenum'><option>4/3</option></select>"));
        assertThrows(
                ErrorQueryConditionException.class, () -> service.collectionQuery(4, "keyword"));
    }

    @Test
    void missingResultsAreEmptyAndMalformedHtmlIsDomainError() throws Exception {
        when(library.fetchCollectionListPage(1, "empty")).thenReturn(Jsoup.parse(""));
        assertNull(service.collectionQuery(1, "empty"));
        when(library.fetchCollectionListPage(1, "bad"))
                .thenReturn(Jsoup.parse("<select id='pagenum'><option>bad</option></select>"));
        assertThrows(ServerErrorException.class, () -> service.collectionQuery(1, "bad"));
    }

    @Test
    void detailUrlDefaultsAndDecodedParametersReachLibrary() throws Exception {
        StringBuilder html =
                new StringBuilder(
                        "<div class='tit'><h1>Book</h1><p>作者：Author</p></div><div"
                                + " class='catalog'>");
        for (String text :
                new String[] {
                    "Principal", "Publisher", "10", "100 pages", "Personal", "Subject", "TP"
                }) html.append("<p>标签：").append(text).append("</p>");
        html.append(
                "</div><table"
                    + " class='tableLib'><tr><td>Barcode</td><td>Call</td><td>Location</td><td>Available</td></tr></table>");
        when(library.fetchCollectionDetailPage(
                        "book/path", "keyword value", "705", "title", "1", "3"))
                .thenReturn(Jsoup.parse(html.toString()));
        var r = service.getCollectionDetailByDetailURL("opacUrl=book%2Fpath&search=keyword+value");
        assertEquals("Book", r.getBookname());
        assertEquals("Author", r.getAuthor());
        assertEquals("Publisher", r.getPublishingHouse());
        assertEquals("TP", r.getChineseLibraryClassification());
        assertEquals("Available", r.getCollectionDistributionList().get(0).getState());
    }

    @Test
    void missingOrIncompleteDetailQueryDoesNotFetch() throws Exception {
        assertNull(service.getCollectionDetailByDetailURL(null));
        assertNull(service.getCollectionDetailByDetailURL(" "));
        assertNull(service.getCollectionDetailByDetailURL("opacUrl=book"));
        verifyNoInteractions(library);
    }

    @Test
    void explicitDetailOptionsArePreservedAndUnavailableDetailIsEmpty() throws Exception {
        when(library.fetchCollectionDetailPage("b", "k", "1", "author", "2", "4"))
                .thenReturn(Jsoup.parse(""));
        assertNull(
                service.getCollectionDetailByDetailURL(
                        "opacUrl=b&search=k&schoolId=1&searchtype=author&page=2&xc=4"));
    }

    @Test
    void networkTimeoutRemainsTyped() throws Exception {
        when(library.fetchCollectionListPage(1, "keyword")).thenThrow(new java.io.IOException());
        assertThrows(NetWorkTimeoutException.class, () -> service.collectionQuery(1, "keyword"));
        when(library.fetchCollectionDetailPage(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString()))
                .thenThrow(new java.io.IOException());
        assertThrows(
                NetWorkTimeoutException.class,
                () -> service.getCollectionDetailByDetailURL("opacUrl=b&search=k"));
    }
}
