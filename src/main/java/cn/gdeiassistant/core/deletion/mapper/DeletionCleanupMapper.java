package cn.gdeiassistant.core.deletion.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;

public interface DeletionCleanupMapper {
    @Insert("insert into account_deletion_cleanup (id,username,resetname,user_id,status,attempts,next_attempt_at) " +
            "values(#{id},#{username},#{resetname},#{userId},'PENDING',0,now())")
    void enqueue(@Param("id") String id, @Param("username") String username,
            @Param("resetname") String resetname, @Param("userId") Long userId);

    @Select("select id from account_deletion_cleanup where (status='PENDING' and next_attempt_at<=now()) " +
            "or (status='PROCESSING' and locked_until<now()) order by next_attempt_at limit 10")
    List<String> pendingIds();

    @Update("update account_deletion_cleanup set status='PROCESSING',locked_until=date_add(now(),interval 10 minute)," +
            "attempts=attempts+1 where id=#{id} and ((status='PENDING' and next_attempt_at<=now()) " +
            "or (status='PROCESSING' and locked_until<now()))")
    int claim(String id);

    @Select("select id,username,resetname,user_id as userId from account_deletion_cleanup where id=#{id}")
    CleanupTask find(String id);

    @Select("select id from account_deletion_cleanup where username=#{username} and status in ('PENDING','PROCESSING') order by id")
    List<String> unfinishedIdsForUsername(String username);

    // Lock by primary key so one username's slow cleanup cannot lock the whole queue.
    @Select("select id from account_deletion_cleanup where id=#{id} and status in ('PENDING','PROCESSING') for update")
    String lockUnfinished(String id);

    @Update("update account_deletion_cleanup set status='DONE',username=null,locked_until=null,error_code=null " +
            "where id=#{id}")
    void complete(String id);

    @Update("update account_deletion_cleanup set status='PENDING',locked_until=null," +
            "next_attempt_at=date_add(now(),interval 5 minute),error_code=#{errorCode} where id=#{id}")
    void retry(@Param("id") String id, @Param("errorCode") String errorCode);

    class CleanupTask {
        private String id, username, resetname;
        private Long userId;
        public String getId() { return id; }
        public void setId(String id) { this.id=id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username=username; }
        public String getResetname() { return resetname; }
        public void setResetname(String resetname) { this.resetname=resetname; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId=userId; }
    }
}
