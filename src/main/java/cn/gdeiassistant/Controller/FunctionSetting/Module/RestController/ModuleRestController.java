package cn.gdeiassistant.Controller.FunctionSetting.Module.RestController;

import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Tools.SpringUtils.ModuleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModuleRestController {

    @Autowired
    private ModuleUtils moduleUtils;

    /**
     * 检查功能模块启用状态
     *
     * @return
     */
    @RequestMapping(value = "/api/module/state", method = RequestMethod.GET)
    public DataJsonResult<String> GetModuleState() {
        return new DataJsonResult<>(true, moduleUtils.PrintModuleStateLog());
    }

    /**
     * 检查核心功能模块启用状态
     *
     * @return
     */
    @RequestMapping(value = "/api/module/core/state", method = RequestMethod.GET)
    public DataJsonResult<String> GetCoreModuleState() {
        return new DataJsonResult<>(true, moduleUtils.PrintCoreModuleStateLog());
    }
}
