package edu.gdei.gdeiassistant.Controller.BookQuery;

import edu.gdei.gdeiassistant.Enum.Base.ServiceResultEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.Book;
import edu.gdei.gdeiassistant.Pojo.Result.BaseResult;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Service.BookQuery.BookQueryService;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class BookQueryController {

    @Autowired
    private BookQueryService bookQueryService;

    @RequestMapping(value = "/book", method = RequestMethod.GET)
    public ModelAndView ResolveBookPage() {
        return new ModelAndView("Book/book");
    }

    @RequestMapping(value = "/rest/bookrenew", method = RequestMethod.POST)
    @ResponseBody
    public DataJsonResult<String> BookRenew(HttpServletRequest request, @RequestParam("url") String url) {
        DataJsonResult<String> jsonResult = new DataJsonResult<>();
        String number = (String) request.getSession().getAttribute("number");
        if (!StringUtils.isBlank(number)) {
            BaseResult<String, ServiceResultEnum> renewResult = bookQueryService
                    .BookRenew(request.getSession().getId(), url);
            switch (renewResult.getResultType()) {
                case SUCCESS:
                    jsonResult.setData(renewResult.getResultData());
                    jsonResult.setSuccess(true);
                    break;

                case TIME_OUT:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("网络连接超时，请重试");
                    break;

                case SERVER_ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("移动图书馆系统异常，请稍后再试");
                    break;
            }
        } else {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("用户身份凭证已过期，请重新登录");
        }
        return jsonResult;
    }

    @RequestMapping(value = "/rest/bookquery", method = RequestMethod.POST)
    @ResponseBody
    public DataJsonResult<List<Book>> BookQuery(HttpServletRequest request
            , @RequestParam(value = "password") String password) {
        DataJsonResult<List<Book>> jsonResult = new DataJsonResult<>();
        String number = (String) request.getSession().getAttribute("number");
        if (!StringUtils.isBlank(number)) {
            BaseResult<List<Book>, ServiceResultEnum> queryResult = bookQueryService
                    .BookQuery(request.getSession().getId(), number, password);
            switch (queryResult.getResultType()) {
                case SUCCESS:
                    jsonResult.setSuccess(true);
                    break;

                case PASSWORD_INCORRECT:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("借阅证密码错误");
                    break;

                case SERVER_ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("移动图书馆系统异常，请稍后再试");
                    break;

                case TIME_OUT:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("网络连接超时，请重试");
                    break;

                case EMPTY_RESULT:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("没有借阅任何图书");
                    break;
            }
        } else {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("用户身份凭证已过期，请重新登录");
        }
        return jsonResult;
    }
}
