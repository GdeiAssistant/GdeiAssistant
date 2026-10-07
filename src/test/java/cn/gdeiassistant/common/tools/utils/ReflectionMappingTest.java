package cn.gdeiassistant.common.tools.utils;

import cn.gdeiassistant.common.pojo.entity.Entity;
import org.junit.jupiter.api.Test;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/** Mapping contracts used by annotation-based spreadsheet and user-data exports. */
class ReflectionMappingTest {
    public static class Base implements Entity {
        private String inherited="synthetic inherited";
        private String absent;
        public String getInherited(){return inherited;}
        public void setInherited(String inherited){this.inherited=inherited;}
        private String protectedValue(String suffix){return inherited+suffix;}
    }
    public static class Child extends Base {
        private Base nested=new Base();
        public Base getNested(){return nested;}
        public void setNested(Base nested){this.nested=nested;}
        public void fail(){throw new IllegalStateException("synthetic mapping failure");}
    }
    public static class GrandChild extends Child {}
    public static class GenericBase<T> {}
    public static class StringMapping extends GenericBase<String> {}
    @Test void nestedPropertiesAndInheritedPrivateFieldsMapWithoutLosingInvocationCauses(){
        var value=new Child();ReflectionUtils.invokeSetter(value,"nested.inherited","changed");
        assertEquals("changed",ReflectionUtils.invokeGetter(value,"nested.inherited"));
        ReflectionUtils.setFieldValue(value,"inherited","parent updated");assertEquals("parent updated",ReflectionUtils.getFieldValue(value,"inherited"));
        assertEquals("parent updated!",ReflectionUtils.invokeMethod(value,"protectedValue",new Class[]{String.class},new Object[]{"!"}));
        assertEquals("parent updated?",ReflectionUtils.invokeMethodByName(value,"protectedValue",new Object[]{"?"}));
        assertNull(ReflectionUtils.getAccessibleField(value,"unknown"));assertNull(ReflectionUtils.getAccessibleMethod(value,"unknown"));assertNull(ReflectionUtils.getAccessibleMethodByName(value,"unknown"));
        assertThrows(IllegalArgumentException.class,()->ReflectionUtils.getFieldValue(value,"unknown"));assertThrows(IllegalArgumentException.class,()->ReflectionUtils.setFieldValue(value,"unknown",1));
        assertThrows(IllegalArgumentException.class,()->ReflectionUtils.invokeMethod(value,"unknown",new Class[]{},new Object[]{}));assertThrows(IllegalArgumentException.class,()->ReflectionUtils.invokeMethodByName(value,"unknown",new Object[]{}));
        var error=assertThrows(RuntimeException.class,()->ReflectionUtils.invokeMethod(value,"fail",new Class[]{},new Object[]{}));assertInstanceOf(IllegalStateException.class,error.getCause());
        assertEquals(String.class,ReflectionUtils.getClassGenricType(StringMapping.class));assertEquals(Object.class,ReflectionUtils.getClassGenricType(StringMapping.class,9));assertEquals(Object.class,ReflectionUtils.getClassGenricType(Base.class));assertEquals(Child.class,ReflectionUtils.getUserClass(value));
        assertInstanceOf(IllegalArgumentException.class,ReflectionUtils.convertReflectionExceptionToUnchecked(new NoSuchMethodException()));var runtime=new IllegalStateException();assertSame(runtime,ReflectionUtils.convertReflectionExceptionToUnchecked(runtime));assertInstanceOf(RuntimeException.class,ReflectionUtils.convertReflectionExceptionToUnchecked(new java.io.IOException()));
    }
    @Test void userDataSnapshotUsesNestedEntityFieldsAndIncludesInheritedValues(){
        var snapshot=ReflectionUtils.getAllNotNullObjectFields(new GrandChild(),GrandChild.class);
        assertEquals("synthetic inherited",snapshot.get("inherited"));assertFalse(snapshot.containsKey("absent"));
        assertEquals(Map.of("inherited","synthetic inherited"),snapshot.get("nested"));
    }
}
