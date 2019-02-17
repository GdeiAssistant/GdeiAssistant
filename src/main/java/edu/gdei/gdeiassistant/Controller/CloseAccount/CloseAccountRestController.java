package edu.gdei.gdeiassistant.Controller.CloseAccount;

import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.CloseAccount.CloseAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
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
    @ResponseBody
    public JsonResult CloseAccount(HttpServletRequest request, String password) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        closeAccountService.CloseAccount(username, password);
        return new JsonResult(true);
    }
}
