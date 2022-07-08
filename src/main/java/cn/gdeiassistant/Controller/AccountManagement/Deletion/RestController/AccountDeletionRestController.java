package cn.gdeiassistant.Controller.AccountManagement.Deletion.RestController;

import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.AccountManagement.Deletion.AccountDeletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class AccountDeletionRestController {

    @Autowired
    private AccountDeletionService accountDeletionService;

    /**
     * 删除账号
     *
     * @param request
     * @param password
     * @return
     */
    @RequestMapping(value = "/api/close/submit", method = RequestMethod.POST)
    public JsonResult DeleteAccount(HttpServletRequest request, String password) throws Exception {
        //检查是否符合删除账号条件
        DataJsonResult<Map<String,String>> result = accountDeletionService.CheckAccountDeletability(request.getSession().getId(), password);
        if (result.isSuccess()) {
            //符合删除账号条件，进行账号删除
            accountDeletionService.DeleteAccount(request.getSession().getId());
        }
        return result;
    }
}
