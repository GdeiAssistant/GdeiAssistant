package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.pojo.Entity.Book;
import cn.gdeiassistant.common.pojo.Entity.Collection;
import cn.gdeiassistant.common.pojo.Entity.CollectionDetail;
import cn.gdeiassistant.common.pojo.Entity.CollectionDistribution;
import cn.gdeiassistant.core.bookquery.service.BookQueryService;
import cn.gdeiassistant.core.collectionquery.pojo.CollectionQueryResult;
import cn.gdeiassistant.core.collectionquery.service.CollectionQueryService;
import cn.gdeiassistant.core.library.controller.LibraryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LibraryControllerContractTest {

    private MockMvc mockMvc;
    private BookQueryService bookQueryService;
    private CollectionQueryService collectionQueryService;

    @BeforeEach
    void setUp() {
        LibraryController controller = new LibraryController();
        bookQueryService = mock(BookQueryService.class);
        collectionQueryService = mock(CollectionQueryService.class);
        ReflectionTestUtils.setField(controller, "bookQueryService", bookQueryService);
        ReflectionTestUtils.setField(controller, "collectionQueryService", collectionQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void searchReturnsCanonicalSearchPayload() throws Exception {
        Collection collection = new Collection();
        collection.setBookname("算法导论");
        collection.setAuthor("Thomas H. Cormen");
        collection.setPublishingHouse("机械工业出版社");
        collection.setDetailURL("https://library.example/detail/algorithms");

        CollectionQueryResult result = new CollectionQueryResult();
        result.setSumPage(3);
        result.setCollectionList(List.of(collection));

        when(collectionQueryService.collectionQuery(1, "算法")).thenReturn(result);

        mockMvc.perform(get("/api/library/search")
                        .param("keyword", "算法")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        ContractResourceSupport.loadJson("contracts/library-search.success.json")
                ));
    }

    @Test
    void detailReturnsCanonicalDetailPayload() throws Exception {
        CollectionDistribution distribution = new CollectionDistribution();
        distribution.setLocation("图书馆四楼");
        distribution.setCallNumber("TP301.6/C67");
        distribution.setBarcode("A00001");
        distribution.setState("在馆");

        CollectionDetail detail = new CollectionDetail();
        detail.setBookname("算法导论");
        detail.setAuthor("Thomas H. Cormen");
        detail.setPrincipal("Thomas H. Cormen");
        detail.setPublishingHouse("机械工业出版社");
        detail.setPrice("128.00");
        detail.setPhysicalDescriptionArea("730页");
        detail.setPersonalPrincipal("Thomas H. Cormen 等著");
        detail.setSubjectTheme("算法设计");
        detail.setChineseLibraryClassification("TP301.6");
        detail.setCollectionDistributionList(List.of(distribution));

        when(collectionQueryService.getCollectionDetailByDetailURL("https://library.example/detail/algorithms"))
                .thenReturn(detail);

        mockMvc.perform(get("/api/library/detail")
                        .param("detailURL", "https://library.example/detail/algorithms"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        ContractResourceSupport.loadJson("contracts/library-detail.success.json")
                ));
    }

    @Test
    void getBorrowReturnsCanonicalBorrowPayload() throws Exception {
        Book book = new Book();
        book.setId("BK-001");
        book.setSn("SN-001");
        book.setCode("CODE-001");
        book.setName("计算机网络");
        book.setAuthor("谢希仁");
        book.setBorrowDate("2026-03-01");
        book.setReturnDate("2026-03-28");
        book.setRenewTime(1);

        when(bookQueryService.getBorrowedBooks("session-1", "library-pass"))
                .thenReturn(List.of(book));

        mockMvc.perform(get("/api/library/borrow")
                        .requestAttr("sessionId", "session-1")
                        .param("password", "library-pass"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        ContractResourceSupport.loadJson("contracts/library-borrow.success.json")
                ));

        verify(bookQueryService).getBorrowedBooks("session-1", "library-pass");
    }

    @Test
    void renewRejectsMissingPasswordWithStableValidationPayload() throws Exception {
        mockMvc.perform(post("/api/library/renew")
                        .requestAttr("sessionId", "session-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sn": "SN-001",
                                  "code": "CODE-001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        ContractResourceSupport.loadJson("contracts/library-renew.validation-failure.json")
                ));

        verifyNoInteractions(bookQueryService);
    }

    @Test
    void renewReturnsCanonicalSuccessPayload() throws Exception {
        mockMvc.perform(post("/api/library/renew")
                        .requestAttr("sessionId", "session-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sn": "SN-001",
                                  "code": "CODE-001",
                                  "password": "library-pass"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        ContractResourceSupport.loadJson("contracts/library-renew.success.json")
                ));

        verify(bookQueryService).bookquery("session-1", "library-pass");
        verify(bookQueryService).renewBook("session-1", "SN-001", "CODE-001");
    }
}
