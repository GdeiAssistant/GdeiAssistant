package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs.IPAddress;

import cn.gdeiassistant.Pojo.Entity.IPAddressRecord;
import org.apache.ibatis.annotations.*;

public interface IPAddressMapper {

    @Select("select * from ip_log where username=#{username} order by time desc limit 1")
    @Results(id = "IPAddressRecord", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "type", property = "type"),
            @Result(column = "username", property = "username"),
            @Result(column = "ip", property = "ip"),
            @Result(column = "country", property = "country"),
            @Result(column = "province", property = "province"),
            @Result(column = "city", property = "city"),
            @Result(column = "time", property = "time")
    })
    IPAddressRecord selectLatestIPAddressRecord(String username);

    @Select("select * from ip_log where username=#{username} and type=#{type} order by time desc limit 1")
    @ResultMap("IPAddressRecord")
    IPAddressRecord selectLatestIPAddressRecordByType(@Param("username") String username, @Param("type") int type);

    @Select("select * from ip_log where username=#{username} order by time desc")
    @ResultMap("IPAddressRecord")
    IPAddressRecord selectAllIPAddressRecord(String username);

    @Insert("insert into ip_log (type,username,ip,country,province,city,time)" +
            " values(#{type},#{username},#{ip},#{country},#{province},#{city},now())")
    void insertIPAddressRecord(IPAddressRecord ipAddressRecord);
}
