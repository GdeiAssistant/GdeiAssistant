package cn.gdeiassistant.core.module.controller;

import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.tools.SpringUtils.ModuleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModuleController {

    @Autowired
    private ModuleUtils moduleUtils;

    /**
     * 检查功能模块启用状态
     *
     * @return
     */
    @RequestMapping(value = "/api/module/state", method = RequestMethod.GET)
    public DataJsonResult<String> getModuleState() {
        return new DataJsonResult<>(true, moduleUtils.PrintModuleStateLog());
    }

    /**
     * 检查核心功能模块启用状态
     *
     * @return
     */
    @RequestMapping(value = "/api/module/core/state", method = RequestMethod.GET)
    public DataJsonResult<String> getCoreModuleState() {
        return new DataJsonResult<>(true, moduleUtils.PrintCoreModuleStateLog());
    }
}
