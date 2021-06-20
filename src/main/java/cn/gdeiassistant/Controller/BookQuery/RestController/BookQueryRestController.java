package cn.gdeiassistant.Controller.BookQuery.RestController;

import cn.gdeiassistant.Annotation.RestAuthentication;
import cn.gdeiassistant.Annotation.RestQueryLogPersistence;
import cn.gdeiassistant.Annotation.TrialData;
import cn.gdeiassistant.Pojo.Entity.Book;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.BookQuery.BookQueryService;
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
    @TrialData(value = "book")
    public DataJsonResult<List<Book>> BookQuery(HttpServletRequest request
            , @RequestParam(value = "password") String password) throws Exception {
        String number = bookQueryService.UpdateSessionAndGetUserNumber(request.getSession().getId()
                , (String) request.getSession().getAttribute("username")
                , (String) request.getSession().getAttribute("password"));
        List<Book> bookList = bookQueryService.BookQuery(request.getSession().getId(), number, password);
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
    @TrialData(value = "book")
    public DataJsonResult<List<Book>> RestBookQuery(HttpServletRequest request
            , @RequestParam("token") String token, @RequestParam(value = "password") String password) throws Exception {
        User user = (User) request.getAttribute("user");
        String number = bookQueryService.UpdateSessionAndGetUserNumber(request.getSession().getId()
                , user.getUsername(), user.getPassword());
        List<Book> bookList = bookQueryService.BookQuery(request.getSession().getId(), number, password);
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
    @TrialData(value = "bookrenew")
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
    @TrialData(value = "bookrenew")
    public JsonResult RestBookRenew(HttpServletRequest request, @RequestParam("token") String token
            , @Validated @NotBlank @RequestParam(value = "password") String password
            , @Validated @NotBlank String code, @Validated @NotBlank String sn) throws Exception {
        User user = (User) request.getAttribute("user");
        String number = bookQueryService.UpdateSessionAndGetUserNumber(request.getSession().getId()
                , user.getUsername(), user.getPassword());
        bookQueryService.BookQuery(request.getSession().getId(), number, password);
        bookQueryService.BookRenew(request.getSession().getId(), sn, code);
        return new JsonResult(true);
    }
}
