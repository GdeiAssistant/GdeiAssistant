package cn.gdeiassistant.Tools.SpringUtils;

import cn.gdeiassistant.Enum.Module.CoreModuleEnum;
import cn.gdeiassistant.Enum.Module.ModuleEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class ModuleUtils {

    @Resource(name = "moduleStateMap")
    private Map<ModuleEnum, Boolean> moduleStateMap;

    @Resource(name = "coreModuleStateMap")
    private Map<CoreModuleEnum, Boolean> coreModuleEnumBooleanMap;

    /**
     * 标记功能模块已停用
     *
     * @param moduleEnum
     */
    public void DisableModule(ModuleEnum moduleEnum) {
        moduleStateMap.put(moduleEnum, false);
    }

    /**
     * 标记核心功能模块已停用
     *
     * @param coreModuleEnum
     */
    public void DisableCoreModule(CoreModuleEnum coreModuleEnum) {
        coreModuleEnumBooleanMap.put(coreModuleEnum, false);
    }

    /**
     * 标记功能模块已启用
     *
     * @param moduleEnum
     */
    public void EnableModule(ModuleEnum moduleEnum) {
        moduleStateMap.put(moduleEnum, true);
    }

    /**
     * 标记核心功能模块已启用
     *
     * @param coreModuleEnum
     */
    public void EnableCoreModule(CoreModuleEnum coreModuleEnum) {
        coreModuleEnumBooleanMap.put(coreModuleEnum, true);
    }

    /**
     * 检查功能模块状态
     *
     * @param moduleEnum
     * @return
     */
    public boolean CheckModuleState(ModuleEnum moduleEnum) {
        if (moduleStateMap.containsKey(moduleEnum)) {
            return moduleStateMap.get(moduleEnum);
        }
        return true;
    }

    /**
     * 检查核心功能模块状态
     *
     * @param coreModuleEnum
     * @return
     */
    public boolean CheckCoreModuleState(CoreModuleEnum coreModuleEnum) {
        if (moduleStateMap.containsKey(coreModuleEnum)) {
            return moduleStateMap.get(coreModuleEnum);
        }
        return true;
    }

    /**
     * 输出核心功能模块状态日志
     *
     * @return
     */
    public String PrintCoreModuleStateLog() {
        //未启用的核心功能模块提示信息
        StringBuilder coreMessage = new StringBuilder();
        //未启用的核心功能模块配置文件路径提示信息
        StringBuilder coreLocations = new StringBuilder();
        for (Map.Entry<CoreModuleEnum, Boolean> entry : coreModuleEnumBooleanMap.entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue())) {
                if (coreMessage.length() == 0) {
                    coreMessage.append("以下的核心功能模块未启用：");
                    coreMessage.append(entry.getKey().getName());
                    coreLocations.append("\n其路径分别为resources资源文件路径下config目录的：");
                    coreLocations.append(entry.getKey().getLocation());
                } else {
                    coreMessage.append("、").append(entry.getKey().getName());
                    coreLocations.append("、").append(entry.getKey().getLocation());
                }
            }
        }
        if (coreMessage.length() != 0) {
            coreMessage.append("。必须启用核心功能模块才能正常使用系统，请完善相关配置文件。");
        }
        //整合功能模块提示信息和功能模块配置文件路径提示信息作为提示文本
        return coreMessage.toString() + coreLocations.toString();
    }

    /**
     * 输出功能模块状态日志
     *
     * @return
     */
    public String PrintModuleStateLog() {
        //未启用的功能模块提示信息
        StringBuilder message = new StringBuilder();
        //未启用的功能模块配置文件路径提示信息
        StringBuilder locations = new StringBuilder();
        for (Map.Entry<ModuleEnum, Boolean> entry : moduleStateMap.entrySet()) {
            if (Boolean.FALSE.equals(entry.getValue())) {
                if (message.length() == 0) {
                    message.append("以下的扩展功能模块未启用：");
                    message.append(entry.getKey().getName());
                    locations.append("\n其路径分别为resources资源文件路径下config目录的：");
                    locations.append(entry.getKey().getLocation());
                } else {
                    message.append("、").append(entry.getKey().getName());
                    locations.append("、").append(entry.getKey().getLocation());
                }
            }
        }
        if (message.length() != 0) {
            message.append("。部分功能可能无法使用，若需要使用对应功能，请完善相关配置文件。");
        }
        //整合功能模块提示信息和功能模块配置文件路径提示信息作为提示文本
        return message.toString() + locations.toString();
    }
}
