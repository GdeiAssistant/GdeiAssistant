package cn.gdeiassistant.core.wechat.controller;

import cn.gdeiassistant.common.pojo.Entity.WechatAccount;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.tools.Utils.WechatAccountUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 校园公众号列表接口。
 * 公开只读，无登录与试用用户限制。
 */
@RestController
@RequestMapping("/api/wechat/account")
public class WechatAccountController {

    /**
     * 加载全部校园公众号列表。
     * GET /api/wechat/account/list
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public DataJsonResult<List<WechatAccount>> LoadWechatAccountList() {
        List<WechatAccount> accountList = WechatAccountUtils.getWechatAccountList();
        return new DataJsonResult<>(true, accountList);
    }
}

