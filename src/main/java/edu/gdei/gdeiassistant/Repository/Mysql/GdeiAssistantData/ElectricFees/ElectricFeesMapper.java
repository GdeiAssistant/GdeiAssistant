package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistantData.ElectricFees;

import edu.gdei.gdeiassistant.Pojo.Entity.ElectricFees;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface ElectricFeesMapper {

    @Select("select * from electricFees where name=#{name} and number=#{number} and year=#{year}")
    @Results(id = "ElectricFees", value = {
            @Result(property = "year", column = "year"),
            @Result(property = "building_number", column = "buildingNumber"),
            @Result(property = "room_number", column = "roomNumber"),
            @Result(property = "people_number", column = "peopleNumber"),
            @Result(property = "department", column = "department"),
            @Result(property = "number", column = "number"),
            @Result(property = "name", column = "name"),
            @Result(property = "used_electric_amount", column = "usedElectricAmount"),
            @Result(property = "free_electric_amount", column = "freeElectricAmount"),
            @Result(property = "fee_based_electric_amount", column = "feeBasedElectricAmount"),
            @Result(property = "electric_price", column = "electricPrice"),
            @Result(property = "total_electric_bill", column = "totalElectricBill"),
            @Result(property = "average_electric_bill", column = "averageElectricBill")
    })
    public ElectricFees selectElectricFees(@Param("name") String name
            , @Param("number") Long number, @Param("year") Integer year);

    @Insert("insert into electricFees (year,building_number,room_number,people_number" +
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
