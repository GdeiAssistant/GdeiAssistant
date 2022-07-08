package cn.gdeiassistant.Controller.AcademicAffairs.BookQuery.RestController;

import cn.gdeiassistant.Annotation.RestAuthentication;
import cn.gdeiassistant.Annotation.RestQueryLogPersistence;
import cn.gdeiassistant.Annotation.TrialData;
import cn.gdeiassistant.Pojo.Entity.Book;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.AcademicAffairs.BookQuery.BookQueryService;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class BookQueryRestController {

    @Autowired
    private BookQueryService bookQueryService;

    /**
     * 查询借阅的图书
     *
     * @param request
     * @param password
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/bookquery", method = RequestMethod.POST)
    @TrialData(value = "book", rest = false)
    public DataJsonResult<List<Book>> BookQuery(HttpServletRequest request
            , @RequestParam(value = "password") String password) throws Exception {
        List<Book> bookList = bookQueryService.BookQuery(request.getSession().getId(), password);
        return new DataJsonResult<>(true, bookList);
    }

    /**
     * 查询借阅的图书Restful接口
     *
     * @param request
     * @param password
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/rest/bookquery", method = RequestMethod.POST)
    @RestAuthentication
    @RestQueryLogPersistence
    @TrialData(value = "book", rest = true)
    public DataJsonResult<List<Book>> RestBookQuery(HttpServletRequest request
            , @RequestParam("token") String token, @RequestParam(value = "password") String password) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        List<Book> bookList = bookQueryService.BookQuery(sessionId, password);
        return new DataJsonResult<>(true, bookList);
    }

    /**
     * 图书续借
     *
     * @param request
     * @param code
     * @param sn
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/bookrenew", method = RequestMethod.POST)
    @TrialData(value = "bookrenew", rest = false)
    public JsonResult BookRenew(HttpServletRequest request, @Validated @NotBlank String code, @Validated @NotBlank String sn) throws Exception {
        bookQueryService.BookRenew(request.getSession().getId(), sn, code);
        return new JsonResult(true);
    }

    /**
     * 图书续借Restful接口
     *
     * @param request
     * @param password
     * @param code
     * @param sn
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/rest/bookrenew", method = RequestMethod.POST)
    @RestAuthentication
    @RestQueryLogPersistence
    @TrialData(value = "bookrenew", rest = true)
    public JsonResult RestBookRenew(HttpServletRequest request, @RequestParam("token") String token
            , @Validated @NotBlank @RequestParam(value = "password") String password
            , @Validated @NotBlank String code, @Validated @NotBlank String sn) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        bookQueryService.BookQuery(sessionId, password);
        bookQueryService.BookRenew(sessionId, sn, code);
        return new JsonResult(true);
    }
}
