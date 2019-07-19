package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.SQLBuilder;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

public class DataSQLBuilder {

    public String selectUserProfile(String username) {
        return new SQL() {{
            SELECT("p.kickname,p.gender,p.birthday,p.degree,p.faculty,p.major,p.region,p.state,p.city");
            SELECT("p.enrollment,p.primary_school,p.junior_high_school,p.high_school");
            SELECT("g.gender as custom_gender");
            FROM("profile p");
            LEFT_OUTER_JOIN("gender g on p.username = g.username");
            WHERE("p.username=#{username}");
        }}.toString();
    }

    public String selectUserIntroduction(String username) {
        return new SQL() {{
            SELECT("i.introduction");
            FROM("introduction i");
            WHERE("username=#{username}");
        }}.toString();
    }

    public String selectUserPrivacy(String username) {
        return new SQL() {{
            SELECT("p.is_gender_open,p.is_region_open,p.is_introduction_open");
            SELECT("p.is_enrollment_open,p.is_primary_school_open,p.is_junior_high_school_open,p.is_high_school_open");
            SELECT("p.is_faculty_open,p.is_major_open,p.is_cache_allow,p.is_robots_index_allow");
            FROM("privacy p");
            WHERE("username=#{username}");
        }}.toString();
    }

    public String selectUserGraduation(String username) {
        return new SQL() {{
            SELECT("g.program");
            FROM("graduation g");
            WHERE("username=#{username}");
        }}.toString();
    }

    public String selectUserCetNumber(String username) {
        return new SQL() {{
            SELECT("c.number");
            FROM("cet c");
            WHERE("username=#{username}");
        }}.toString();
    }

    public String selectUserAuthentication(String username) {
        return new SQL() {{
            SELECT("auth.identity_code,auth.realname,auth.identity_number,auth.school_number");
            FROM("authentication auth");
            WHERE("username=#{username}");
        }}.toString();
    }

    public String selectUserDeliveryOrderList(String username) {
        return new SQL() {{
            SELECT("do.order_id,do.order_time,do.name,do.number,do.phone,do.price,do.company");
            SELECT("do.address,do.state,do.remarks");
            FROM("delivery_order do");
            WHERE("username=#{username}");
        }}.toString();
    }

    public String selectUserDeliveryTradeList(String username) {
        return new SQL() {{
            SELECT("dt.trade_id,dt.order_id,dt.create_time,dt.state");
            FROM("delivery_trade dt");
            WHERE("username=#{username}");
        }}.toString();
    }

    public String selectUserErshouItemList(String username) {
        return new SQL() {{
            SELECT("es.id,es.name,es.description,es.price,es.location,es.type,es.qq,es.phone,es.state,es.publish_time");
            FROM("ershou es");
            WHERE("username=#{username}");
        }}.toString();
    }

    public String selectUserLostAndFoundItemList(String username) {
        return new SQL() {{
            SELECT("laf.id,laf.name,laf.description,laf.location,laf.item_type,laf.lost_type,laf.qq,laf.wechat,laf.phone,laf.state,laf.publish_time");
            FROM("lostandfound laf");
            WHERE("username=#{username}");
        }}.toString();
    }

    public String selectUserSecretItemList(String username) {
        return new SQL() {{
            SELECT("sc.id,sc.content,sc.theme,sc.type,sc.timer,sc.state,sc.publish_time,count(scl.id) as like_count");
            FROM("secret_content sc");
            LEFT_OUTER_JOIN("secret_like scl using (id)");
            WHERE("sc.username=#{username}");
            GROUP_BY("sc.id,sc.content,sc.theme,sc.type,sc.timer,sc.state,sc.publish_time,scl.id");
        }}.toString();
    }

    public String selectSecretCommentList(@Param("id") int id) {
        return new SQL() {{
            SELECT("scc.id as id,scc.comment as comment,scc.publish_time as publish_time,scc.avatar_theme as avatar_theme");
            FROM("secret_comment scc");
            WHERE("scc.id=#{id}");
        }}.toString();
    }
}
