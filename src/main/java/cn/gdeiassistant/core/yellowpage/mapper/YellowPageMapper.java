package cn.gdeiassistant.core.yellowPage.mapper;

import cn.gdeiassistant.core.dataquery.pojo.YellowPageType;
import cn.gdeiassistant.common.pojo.Entity.YellowPage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface YellowPageMapper {

    @Select("select * from gdeiassistant_data.yellow_page join gdeiassistant_data.yellow_page_type on gdeiassistant_data.yellow_page.type_code = gdeiassistant_data.yellow_page_type.type_code")
    @Results(id = "YellowPage", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "typeCode", column = "type_code"),
            @Result(property = "typeName", column = "type_name"),
            @Result(property = "section", column = "section"),
            @Result(property = "campus", column = "campus"),
            @Result(property = "majorPhone", column = "major_phone"),
            @Result(property = "minorPhone", column = "minor_phone"),
            @Result(property = "address", column = "address"),
            @Result(property = "email", column = "email"),
            @Result(property = "website", column = "website")
    })
    List<YellowPage> selectAllYellowPageList();

    @Select("select * from gdeiassistant_data.yellow_page_type")
    @Results(id = "YellowPageType", value = {
            @Result(property = "typeCode", column = "type_code"),
            @Result(property = "typeName", column = "type_name")
    })
    List<YellowPageType> selectYellowPageType();

    @Insert("insert into gdeiassistant_data.yellow_page (type_code,section,campus,major_phone,minor_phone,address,email,website) values " +
            "<foreach collection='list' item='yellowPage' index='index' separator=','> " +
            "(#{yellowPage.typeCode},#{yellowPage.section},#{yellowPage.campus} " +
            ",#{yellowPage.majorPhone},#{yellowPage.minorPhone} " +
            ",#{yellowPage.address},#{yellowPage.email},#{yellowPage.website}) " +
            "</foreach>")
    void insertYellowPageBatch(List<YellowPage> yellowPageList);
}
