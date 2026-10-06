package cn.gdeiassistant.core.user.mapper;

import cn.gdeiassistant.common.pojo.alias.DataEncryption;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

public interface UserMapper {

    @Select("<script>" +
            "select cc.campus_username as username from campus_credential cc " +
            "inner join app_user au on au.id = cc.user_id " +
            "where au.status = 'ACTIVE' and cc.campus_username in " +
            "<foreach item='u' collection='usernames' open='(' separator=',' close=')'>" +
            "#{u}" +
            "</foreach>" +
            "</script>")
    List<String> selectExistingUsernames(@Param("usernames") List<String> usernames);

    @Select("select au.id, au.public_id, au.status, au.created_at, au.updated_at, " +
            "cc.campus_username as username, cc.password " +
            "from campus_credential cc " +
            "inner join app_user au on au.id = cc.user_id " +
            "where cc.campus_username=#{username} limit 1")
    @Results(id = "CampusAccountView", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "publicId", column = "public_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password", javaType = DataEncryption.class, jdbcType = JdbcType.VARCHAR)
    })
    CampusAccountView selectUser(String username);

    @Select("select au.id, au.public_id, au.status, au.created_at, au.updated_at, " +
            "cc.campus_username as username, cc.password " +
            "from app_user au " +
            "left join campus_credential cc on cc.user_id = au.id " +
            "where au.public_id=#{publicId} limit 1")
    @ResultMap("CampusAccountView")
    CampusAccountView selectUserByPublicId(@Param("publicId") String publicId);

    @Select("select au.id, au.public_id, au.status, au.created_at, au.updated_at, " +
            "cc.campus_username as username, cc.password " +
            "from app_user au " +
            "left join campus_credential cc on cc.user_id = au.id " +
            "where au.id=#{id} limit 1")
    @ResultMap("CampusAccountView")
    CampusAccountView selectUserById(@Param("id") Long id);

    /** 事务内行锁（配合 READ_COMMITTED / current read），固定按 id 升序调用避免死锁 */
    @Select("select au.id, au.public_id, au.status, au.created_at, au.updated_at, " +
            "cc.campus_username as username, cc.password " +
            "from app_user au " +
            "left join campus_credential cc on cc.user_id = au.id " +
            "where au.id=#{id} limit 1 for update")
    @ResultMap("CampusAccountView")
    CampusAccountView selectUserByIdForUpdate(@Param("id") Long id);

    @Select("select count(cc.campus_username) from campus_credential cc where cc.campus_username like concat(concat('%',#{username}),'%')")
    @ResultType(Integer.class)
    Integer selectDeletedUserCount(String username);

    @Insert("insert into app_user (public_id, status, created_at, updated_at) " +
            "values (#{publicId}, #{status}, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertAppUser(CampusAccountView user);

    @Insert("insert into campus_credential (user_id, campus_username, password) " +
            "values (#{id}, #{username}, #{password,typeHandler=cn.gdeiassistant.common.typehandler.MybatisEncryptionTypeHandler,jdbcType=VARCHAR})")
    void insertCampusCredential(CampusAccountView user);

    @Update("update campus_credential set password=#{password,typeHandler=cn.gdeiassistant.common.typehandler.MybatisEncryptionTypeHandler,jdbcType=VARCHAR} " +
            "where campus_username=#{username}")
    void updateUser(CampusAccountView user);

    @Update("update campus_credential set password=null where campus_username=#{username}")
    void clearPassword(String username);

    @Update("update app_user set status='CLOSED', updated_at=now() where id=#{id}")
    void closeAppUser(@Param("id") Long id);

    @Update("update campus_credential set campus_username=#{resetname}, password=null where campus_username=#{username}")
    void closeUser(@Param("resetname") String resetname, @Param("username") String username);

    @Select("<script>" +
            "select au.id, au.public_id, au.status, au.created_at, au.updated_at, " +
            "cc.campus_username as username, null as password " +
            "from app_user au " +
            "inner join campus_credential cc on cc.user_id = au.id " +
            "inner join profile p on p.username = cc.campus_username " +
            "where au.status = 'ACTIVE' " +
            "<if test='viewerId != null'>" +
            "and not exists (" +
            "  select 1 from user_block ub where " +
            "  (ub.blocker_id = #{viewerId} and ub.blocked_id = au.id) or " +
            "  (ub.blocker_id = au.id and ub.blocked_id = #{viewerId})" +
            ") " +
            "</if>" +
            "<if test='query != null and query != \"\"'>" +
            "and (p.nickname like concat('%',#{query},'%') or au.public_id = #{query}) " +
            "</if>" +
            "<if test='cursorCreatedAt != null'>" +
            "and (au.created_at &lt; #{cursorCreatedAt} or (au.created_at = #{cursorCreatedAt} and au.id &lt; #{cursorId})) " +
            "</if>" +
            "order by au.created_at desc, au.id desc limit #{limit}" +
            "</script>")
    @ResultMap("CampusAccountView")
    List<CampusAccountView> searchActiveUsers(@Param("query") String query,
                                       @Param("viewerId") Long viewerId,
                                       @Param("cursorCreatedAt") java.util.Date cursorCreatedAt,
                                       @Param("cursorId") Long cursorId,
                                       @Param("limit") int limit);
}
