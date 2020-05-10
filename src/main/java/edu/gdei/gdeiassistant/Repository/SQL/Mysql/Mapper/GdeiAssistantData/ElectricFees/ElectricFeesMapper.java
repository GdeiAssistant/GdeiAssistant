package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantData.ElectricFees;

import edu.gdei.gdeiassistant.Pojo.Entity.ElectricFees;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ElectricFeesMapper {

    @Select("select * from electricfees where name=#{name} and number=#{number} and year=#{year}")
    @Results(id = "ElectricFees", value = {
            @Result(column = "year", property = "year"),
            @Result(column = "building_number", property = "buildingNumber"),
            @Result(column = "room_number", property = "roomNumber"),
            @Result(column = "people_number", property = "peopleNumber"),
            @Result(column = "department", property = "department"),
            @Result(column = "number", property = "number"),
            @Result(column = "name", property = "name"),
            @Result(column = "used_electric_amount", property = "usedElectricAmount"),
            @Result(column = "free_electric_amount", property = "freeElectricAmount"),
            @Result(column = "fee_based_electric_amount", property = "feeBasedElectricAmount"),
            @Result(column = "electric_price", property = "electricPrice"),
            @Result(column = "total_electric_bill", property = "totalElectricBill"),
            @Result(column = "average_electric_bill", property = "averageElectricBill")
    })
    public ElectricFees selectElectricFees(@Param("name") String name
            , @Param("number") Long number, @Param("year") Integer year);

    @Insert("insert into electricfees (year,building_number,room_number,people_number" +
            ",department,number,name,used_electric_amount,free_electric_amount" +
            ",fee_based_electric_amount,electric_price,total_electric_bill,average_electric_bill) values " +
            "<foreach collection='list' item='electricFees' index='index' separator=','> " +
            "(#{electricFees.year},#{electricFees.buildingNumber},#{electricFees.roomNumber}" +
            ",#{electricFees.peopleNumber},#{electricFees.department}" +
            ",#{electricFees.number},#{electricFees.name},#{electricFees.usedElectricAmount}" +
            ",#{electricFees.freeElectricAmount},#{electricFees.feeBasedElectricAmount}" +
            ",#{electricFees.electricPrice},#{electricFees.totalElectricBill},#{electricFees.averageElectricBill}) " +
            "</foreach>")
    public void insertElectricFeesBatch(List<ElectricFees> electricFees);
}
