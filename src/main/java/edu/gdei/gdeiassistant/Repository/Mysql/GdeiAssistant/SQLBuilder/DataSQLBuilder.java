package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.SQLBuilder;

import org.apache.ibatis.jdbc.SQL;

public class DataSQLBuilder {

    public String selectUserProfile(String username) {
        return new SQL() {{
            SELECT("p.nickname,p.gender,p.birthday,p.degree,p.faculty,p.major,p.location_region,p.location_state,p.location_city");
            SELECT("p.hometown_region,p.hometown_state,p.hometown_city,p.enrollment,p.profession,p.primary_school,p.junior_high_school,p.high_school");
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
            SELECT("p.is_gender_open,p.is_location_open,p.is_hometown_open,p.is_introduction_open");
            SELECT("p.is_enrollment_open,p.is_age_open,p.is_degree_open,p.is_primary_school_open,p.is_junior_high_school_open,p.is_high_school_open");
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
            SELECT("auth.identity_code,auth.type");
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

    public String selectUserPhotographItemList(String username) {
        return new SQL() {{
            SELECT("p.id,p.title,p.count,p.content,p.type,p.create_time");
            SELECT("count(distinct pl.like_id)as like_count,count(distinct pc.comment_id) as comment_count");
            SELECT("sum(distinct CASE WHEN pl.username=#{username} THEN 1 ELSE 0 END) as liked");
            FROM("photograph p");
            LEFT_OUTER_JOIN("photograph_like pl on p.id=pl.photo_id");
            LEFT_OUTER_JOIN("photograph_comment pc on p.id=pc.photo_id");
            WHERE("p.username=#{username}");
            GROUP_BY("p.id,p.title,p.count,p.content,p.type,p.create_time,pl.like_id,pc.comment_id");
        }}.toString();
    }

    public String selectUserExpressItemList(String username) {
        return new SQL() {{
            SELECT("e.id,e.nickname,e.realname,e.self_gender,e.name,e.content,e.person_gender,e.publish_time");
            SELECT("count(distinct el.id) as like_count,count(distinct em.id) as comment_count,count(distinct eg.id) as guess_sum");
            SELECT("sum(distinct CASE WHEN el.username=#{username} THEN 1 ELSE 0 END) as liked");
            SELECT("sum(distinct CASE WHEN eg.result=1 THEN 1 ELSE 0 END) as guess_count");
            FROM("express e");
            LEFT_OUTER_JOIN("express_like el on e.id=el.express_id");
            LEFT_OUTER_JOIN("express_comment em on e.id=em.express_id");
            LEFT_OUTER_JOIN("express_guess eg on e.id=eg.express_id");
            WHERE("e.username=#{username}");
            GROUP_BY("e.id");
        }}.toString();
    }
}
