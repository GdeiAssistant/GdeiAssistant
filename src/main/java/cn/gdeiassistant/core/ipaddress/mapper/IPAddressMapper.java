package cn.gdeiassistant.core.iPAddress.mapper;

import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface IPAddressMapper {

    @Select("select id,type,username,ip,country,province,city,time from ip_log where username=#{username} order by time desc limit 1")
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

    @Select("select id,type,username,ip,country,province,city,time from ip_log where username=#{username} and type=#{type} order by time desc limit #{start},#{size}")
    @ResultMap("IPAddressRecord")
    List<IPAddressRecord> selectIPAddressRecordByType(@Param("username") String username, @Param("type") int type, @Param("start") int start, @Param("size") int size);

    @Select("select id,type,username,ip,country,province,city,time from ip_log where username=#{username} and type=#{type} order by time desc limit 1")
    @ResultMap("IPAddressRecord")
    IPAddressRecord selectLatestIPAddressRecordByType(@Param("username") String username, @Param("type") int type);

    @Insert("insert into ip_log (type,username,ip,network,country,province,city,time)" +
            " values(#{type},#{username},#{ip},#{network},#{country},#{province},#{city},now())")
    void insertIPAddressRecord(IPAddressRecord ipAddressRecord);
}
