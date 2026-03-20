package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.pojo.Entity.Book;
import cn.gdeiassistant.core.bookquery.service.BookQueryService;
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

    @BeforeEach
    void setUp() {
        LibraryController controller = new LibraryController();
        bookQueryService = mock(BookQueryService.class);
        ReflectionTestUtils.setField(controller, "bookQueryService", bookQueryService);
        ReflectionTestUtils.setField(controller, "collectionQueryService", mock(CollectionQueryService.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
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
}
