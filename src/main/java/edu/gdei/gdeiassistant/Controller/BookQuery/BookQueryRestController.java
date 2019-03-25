package edu.gdei.gdeiassistant.Controller.BookQuery;

import edu.gdei.gdeiassistant.Pojo.Entity.Book;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.BookQuery.BookQueryService;
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
    public DataJsonResult<List<Book>> BookQuery(HttpServletRequest request
            , @RequestParam(value = "password") String password) throws Exception {
        String number = bookQueryService.UpdateSessionAndGetUserNumber(request.getSession().getId()
                , (String) request.getSession().getAttribute("username")
                , (String) request.getSession().getAttribute("password"));
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
    public JsonResult BookRenew(HttpServletRequest request, @Validated @NotBlank String code, @Validated @NotBlank String sn) throws Exception {
        bookQueryService.BookRenew(request.getSession().getId(), sn, code);
        return new JsonResult(true);
    }
}
