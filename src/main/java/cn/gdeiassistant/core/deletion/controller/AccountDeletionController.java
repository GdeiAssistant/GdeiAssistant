package cn.gdeiassistant.core.deletion.controller;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.deletion.service.AccountDeletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class AccountDeletionController {

    @Autowired
    private AccountDeletionService accountDeletionService;

    /**
     * 删除账号（强拦截测试用户）
     *
     * @param request
     * @param password
     * @return
     */
    @RequestMapping(value = "/api/close/submit", method = RequestMethod.POST)
    @TrialData(value = "closeAccount")
    public JsonResult deleteAccount(HttpServletRequest request, String password) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        //检查是否符合删除账号条件
        DataJsonResult<Map<String,String>> result = accountDeletionService.checkAccountDeletability(sessionId, password);
        if (result.isSuccess()) {
            //符合删除账号条件，进行账号删除
            accountDeletionService.deleteAccount(sessionId);
        }
        return result;
    }
}

