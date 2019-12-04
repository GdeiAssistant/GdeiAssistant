package edu.gdei.gdeiassistant.Controller.Express.RestController;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Annotation.RestCheckAuthentication;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.ExpressException.CorrectRecordException;
import edu.gdei.gdeiassistant.Exception.ExpressException.NoRealNameException;
import edu.gdei.gdeiassistant.Pojo.Entity.Express;
import edu.gdei.gdeiassistant.Pojo.Entity.ExpressComment;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Express.ExpressService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class ExpressRestController {

    @Autowired
    private ExpressService expressService;

    /**
     * 分页查询表白信息
     *
     * @param start
     * @param size
     * @return
     * @throws WsgException
     */
    @RequestMapping(value = "/api/express/start/{start}/size/{size}", method = RequestMethod.GET)
    @RestCheckAuthentication(name = "express")
    public DataJsonResult<List<Express>> QueryExpressPage(HttpServletRequest request, @PathVariable("start") int start
            , @PathVariable("size") int size) throws WsgException {
        String username = (String) request.getSession().getAttribute("username");
        List<Express> expressList = expressService.QueryExpressPage(start, size, username);
        return new DataJsonResult<>(true, expressList);
    }

    /**
     * 分页关键词查询表白信息
     *
     * @param request
     * @param keyword
     * @param start
     * @param size
     * @return
     * @throws WsgException
     */
    @RequestMapping(value = "/api/express/keyword/{keyword}/start/{start}/size/{size}", method = RequestMethod.GET)
    @RestCheckAuthentication(name = "express")
    public DataJsonResult<List<Express>> QueryExpressPageByKeyWord(HttpServletRequest request, @PathVariable("keyword") String keyword
            , @PathVariable("start") int start, @PathVariable("size") int size) throws WsgException {
        String username = (String) request.getSession().getAttribute("username");
        List<Express> expressList = expressService.QueryExpressPageByKeyword(start, size, username, keyword);
        return new DataJsonResult<>(true, expressList);
    }

    /**
     * 发布表白信息
     *
     * @param request
     * @param express
     * @return
     * @throws WsgException
     */
    @RequestMapping(value = "/api/express", method = RequestMethod.POST)
    @RestCheckAuthentication(name = "express")
    public JsonResult AddExpress(HttpServletRequest request, @Validated Express express) throws WsgException {
        String username = (String) request.getSession().getAttribute("username");
        expressService.AddExpress(express, username);
        return new JsonResult(true);
    }

    /**
     * 查询表白评论
     *
     * @return
     * @throws WsgException
     */
    @RequestMapping(value = "/api/express/id/{id}/comment", method = RequestMethod.GET)
    @RestCheckAuthentication(name = "express")
    public DataJsonResult<List<ExpressComment>> QueryExpressComment(HttpServletRequest request, @PathVariable("id") Integer id) throws WsgException {
        List<ExpressComment> expressCommentList = expressService.QueryExpressComment(id);
        return new DataJsonResult<>(true, expressCommentList);
    }

    /**
     * 添加表白评论
     *
     * @param request
     * @param id
     * @param comment
     * @return
     * @throws DataNotExistException
     * @throws WsgException
     */
    @RequestMapping(value = "/api/express/id/{id}/comment", method = RequestMethod.POST)
    @RestCheckAuthentication(name = "express")
    public JsonResult AddExpressComment(HttpServletRequest request, @PathVariable("id") Integer id
            , @Validated @NotBlank @Length(min = 1, max = 50) String comment) throws DataNotExistException, WsgException {
        String username = (String) request.getSession().getAttribute("username");
        expressService.AddExpressComment(id, username, comment);
        return new JsonResult(true);
    }

    /**
     * 提交猜一下表白信息
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/api/express/id/{id}/guess", method = RequestMethod.POST)
    @RestCheckAuthentication(name = "express")
    public DataJsonResult<Boolean> GuessExpress(HttpServletRequest request, @PathVariable("id") int id
            , @Validated @NotBlank @Length(min = 1, max = 10) String name) throws DataNotExistException, WsgException, NoRealNameException, CorrectRecordException {
        String username = (String) request.getSession().getAttribute("username");
        boolean result = expressService.GuessExpress(id, username, name);
        return new DataJsonResult<>(true, result);
    }

    /**
     * 点赞表白信息
     *
     * @param request
     * @param id
     * @return
     * @throws WsgException
     */
    @RequestMapping(value = "/api/express/id/{id}/like", method = RequestMethod.POST)
    @RestCheckAuthentication(name = "express")
    public JsonResult LikeExpress(HttpServletRequest request, @PathVariable("id") int id) throws WsgException, DataNotExistException {
        String username = (String) request.getSession().getAttribute("username");
        expressService.LikeExpress(id, username);
        return new JsonResult(true);
    }
}
