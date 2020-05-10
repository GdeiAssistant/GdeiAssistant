package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Delivery;

import edu.gdei.gdeiassistant.Pojo.Entity.DeliveryOrder;
import edu.gdei.gdeiassistant.Pojo.Entity.DeliveryTrade;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface DeliveryMapper {

    @Select("select username from delivery_order where order_id=#{orderId}")
    @ResultType(String.class)
    public String selectDeliveryOrderUsername(Integer orderId);

    @Select("select * from delivery_order where order_id=#{orderId}")
    @Results(id = "DeliveryOrderDetail", value = {
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "orderTime", column = "order_time"),
            @Result(property = "name", column = "name"),
            @Result(property = "number", column = "number"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "price", column = "price"),
            @Result(property = "company", column = "company"),
            @Result(property = "address", column = "address"),
            @Result(property = "state", column = "state"),
            @Result(property = "remarks", column = "remarks"),
    })
    public DeliveryOrder selectDeliveryOrderByOrderId(Integer orderId);

    @Select("select * from delivery_trade where trade_id=#{tradeId}")
    @Results(id = "DeliveryTrade", value = {
            @Result(property = "tradeId", column = "trade_id"),
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "createTime", column = "create_time"),
            @Result(property = "username", column = "username"),
            @Result(property = "state", column = "state"),
    })
    public DeliveryTrade selectDeliveryTradeByTradeId(Integer tradeId);

    @Select("select * from delivery_trade where order_id=#{orderId}")
    @ResultMap("DeliveryTrade")
    public DeliveryTrade selectDeliveryTradeByOrderId(Integer orderId);

    @Select("select username from delivery_order where order_id=#{orderId}")
    @ResultType(String.class)
    public String selectDeliveryOrderUsernameByOrderId(Integer orderId);

    @Select("select username from delivery_trade where order_id=#{orderId}")
    @ResultType(String.class)
    public String selectDeliveryTradeUsernameByOrderId(Integer orderId);

    @Select("select order_id,username,order_time,price,company,address,state from delivery_order where state=0 order by order_time desc limit #{start},#{size}")
    @ResultMap("DeliveryOrder")
    public List<DeliveryOrder> selectDeliveryOrderPage(@Param("start") Integer start, @Param("size") Integer size);

    @Select("select * from delivery_trade where username=#{username}")
    @ResultMap("DeliveryTrade")
    public List<DeliveryTrade> selectDeliveryTradeByUsername(String username);

    @Select("select * from delivery_order where username=#{username}")
    @Results(id = "DeliveryOrder", value = {
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "orderTime", column = "order_time"),
            @Result(property = "price", column = "price"),
            @Result(property = "company", column = "company"),
            @Result(property = "address", column = "address"),
            @Result(property = "state", column = "state")
    })
    public List<DeliveryOrder> selectDeliveryOrderByUsername(String username);

    @Select("select o.order_id as order_id,o.username as username,order_time,price,company,address,o.state from delivery_order o,delivery_trade t where o.order_id = t.order_id and t.username=#{username}")
    @Results(id = "AcceptedDeliveryOrder", value = {
            @Result(property = "orderId", column = "order_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "orderTime", column = "order_time"),
            @Result(property = "price", column = "price"),
            @Result(property = "company", column = "company"),
            @Result(property = "address", column = "address"),
            @Result(property = "state", column = "state")
    })
    public List<DeliveryOrder> selectAcceptedDeliveryOrderByUsername(String username);

    @Insert("insert into delivery_order (username,order_time,name,number,phone,price,company,address,remarks,state) values(#{username}" +
            ",now(),#{name},#{number},#{phone},#{price},#{company},#{address},#{remarks},0)")
    public void insertDeliveryOrder(DeliveryOrder deliveryOrder);

    @Insert("insert into delivery_trade (order_id,create_time,username,state) values(#{orderId},now(),#{username},0)")
    public void insertTradeRecord(DeliveryTrade deliveryTrade);

    @Update("update delivery_order set state=#{state} where order_id=#{id} and state=0")
    public int updateOrderState(@Param("id") int id, @Param("state") int state);

    @Update("update delivery_trade set state=#{state} where trade_id=#{tradeId}")
    public void updateTradeState(@Param("tradeId") Integer tradeId, @Param("state") Integer state);

    @Update("update delivery_order set state=2 where order_id=#{orderId} and state!=1")
    public int deleteOrder(Integer orderId);
}
