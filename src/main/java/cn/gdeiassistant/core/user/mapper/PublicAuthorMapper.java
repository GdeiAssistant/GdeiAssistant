package cn.gdeiassistant.core.user.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;

public interface PublicAuthorMapper {
    record AuthorRow(String username, String authorId, String displayName) {}
    @Select("<script>SELECT cc.campus_username AS username, CASE WHEN au.status='ACTIVE' THEN au.public_id ELSE NULL END AS authorId, " +
            "COALESCE(NULLIF(p.nickname,''),'用户') AS displayName FROM campus_credential cc INNER JOIN app_user au ON au.id=cc.user_id " +
            "LEFT JOIN profile p ON p.username=cc.campus_username WHERE cc.campus_username IN " +
            "<foreach collection='names' item='name' open='(' separator=',' close=')'>#{name}</foreach></script>")
    List<AuthorRow> selectAuthors(@Param("names") List<String> names);
}
