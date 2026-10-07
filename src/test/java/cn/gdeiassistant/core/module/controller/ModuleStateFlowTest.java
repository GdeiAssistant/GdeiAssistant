package cn.gdeiassistant.core.module.controller;
import cn.gdeiassistant.common.enums.module.*;
import cn.gdeiassistant.common.tools.springutils.ModuleUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ModuleStateFlowTest {
    @Test void detailedStateAndDiagnosticsReflectEnableDisableIncludingModulesWithoutConfig(){
        var extensions=new EnumMap<ModuleEnum,Boolean>(ModuleEnum.class);var core=new EnumMap<CoreModuleEnum,Boolean>(CoreModuleEnum.class);
        var utility=new ModuleUtils();ReflectionTestUtils.setField(utility,"moduleStateMap",extensions);ReflectionTestUtils.setField(utility,"coreModuleEnumBooleanMap",core);
        var controller=new ModuleController();ReflectionTestUtils.setField(controller,"moduleUtils",utility);
        assertEquals("",controller.getModuleState().getData());assertEquals("",controller.getCoreModuleState().getData());
        assertTrue(utility.checkModuleState(ModuleEnum.NEWS));assertTrue(utility.checkCoreModuleState(CoreModuleEnum.MYSQL));
        utility.disableModule(ModuleEnum.NEWS);assertTrue(controller.getModuleState().getData().contains("无需额外配置"));
        utility.disableModule(ModuleEnum.EMAIL);utility.disableModule(ModuleEnum.ENCRYPTION);utility.disableCoreModule(CoreModuleEnum.MYSQL);utility.disableCoreModule(CoreModuleEnum.REDIS);
        var data=controller.getModuleStateDetail().getData();assertEquals(false,((Map<?,?>)data.get("extension")).get("EMAIL"));assertEquals(false,((Map<?,?>)data.get("core")).get("MYSQL"));
        assertTrue(controller.getCoreModuleState().getData().contains("spring.datasource"));assertTrue(controller.getModuleState().getData().contains("email.properties"));
        utility.enableModule(ModuleEnum.EMAIL);utility.enableCoreModule(CoreModuleEnum.MYSQL);assertTrue(utility.checkModuleState(ModuleEnum.EMAIL));assertTrue(utility.checkCoreModuleState(CoreModuleEnum.MYSQL));
    }
}
