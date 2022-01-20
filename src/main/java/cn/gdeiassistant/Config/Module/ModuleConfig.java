package cn.gdeiassistant.Config.Module;

import cn.gdeiassistant.Enum.Module.CoreModuleEnum;
import cn.gdeiassistant.Enum.Module.ModuleEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ModuleConfig {

    /**
     * 功能模块状态表，记录各功能模块的启用禁用状态
     *
     * @return
     */
    @Bean("moduleStateMap")
    public Map<ModuleEnum, Boolean> moduleStateMap() {
        Map<ModuleEnum, Boolean> map = new HashMap<>();
        return map;
    }

    /**
     * 核心功能模块状态表，记录各核心功能模块的启用禁用状态
     *
     * @return
     */
    @Bean("coreModuleStateMap")
    public Map<CoreModuleEnum, Boolean> coreModuleEnumStateMap() {
        Map<CoreModuleEnum, Boolean> map = new HashMap<>();
        return map;
    }
}
