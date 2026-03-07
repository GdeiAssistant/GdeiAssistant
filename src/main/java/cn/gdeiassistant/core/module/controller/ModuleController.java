package cn.gdeiassistant.core.module.controller;

import cn.gdeiassistant.common.enums.Module.CoreModuleEnum;
import cn.gdeiassistant.common.enums.Module.ModuleEnum;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.tools.SpringUtils.ModuleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

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
     * 结构化检查功能模块启用状态（便于前端直接消费）
     */
    @RequestMapping(value = "/api/module/state/detail", method = RequestMethod.GET)
    public DataJsonResult<Map<String, Object>> getModuleStateDetail() {
        Map<String, Object> data = new LinkedHashMap<>();

        Map<String, Boolean> extension = new LinkedHashMap<>();
        for (ModuleEnum moduleEnum : ModuleEnum.values()) {
            extension.put(moduleEnum.name(), moduleUtils.CheckModuleState(moduleEnum));
        }

        Map<String, Boolean> core = new LinkedHashMap<>();
        for (CoreModuleEnum coreModuleEnum : CoreModuleEnum.values()) {
            core.put(coreModuleEnum.name(), moduleUtils.CheckCoreModuleState(coreModuleEnum));
        }

        data.put("extension", extension);
        data.put("core", core);
        return new DataJsonResult<>(true, data);
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
