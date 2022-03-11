package cn.gdeiassistant.Controller.Account.RestController;

import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.Account.CloseAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CloseAccountRestController {

    @Autowired
    private CloseAccountService closeAccountService;

    /**
     * 删除账号
     *
     * @param request
     * @param password
     * @return
     */
    @RequestMapping(value = "/api/close/submit", method = RequestMethod.POST)
    public JsonResult CloseAccount(HttpServletRequest request, String password) throws Exception {
        closeAccountService.CloseAccount(request.getSession().getId(), password);
        return new JsonResult(true);
    }
}
