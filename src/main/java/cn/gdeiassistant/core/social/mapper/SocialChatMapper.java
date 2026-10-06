package cn.gdeiassistant.core.social.mapper;

import cn.gdeiassistant.core.social.pojo.entity.ChatMessageEntity;
import cn.gdeiassistant.core.social.pojo.entity.ConversationEntity;
import cn.gdeiassistant.core.social.pojo.entity.ConversationMemberEntity;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

public interface SocialChatMapper {

    @Select("select id, user_low_id, user_high_id, last_seq, last_message_at, created_at " +
            "from conversation where user_low_id=#{low} and user_high_id=#{high} limit 1")
    @Results(id = "Conversation", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "userLowId", column = "user_low_id"),
            @Result(property = "userHighId", column = "user_high_id"),
            @Result(property = "lastSeq", column = "last_seq"),
            @Result(property = "lastMessageAt", column = "last_message_at"),
            @Result(property = "createdAt", column = "created_at")
    })
    ConversationEntity selectConversationByPair(@Param("low") long low, @Param("high") long high);

    @Select("select id, user_low_id, user_high_id, last_seq, last_message_at, created_at from conversation where id=#{id}")
    @ResultMap("Conversation")
    ConversationEntity selectConversationById(@Param("id") long id);

    @Select("select id, user_low_id, user_high_id, last_seq, last_message_at, created_at " +
            "from conversation where id=#{id} for update")
    @ResultMap("Conversation")
    ConversationEntity selectConversationByIdForUpdate(@Param("id") long id);

    @Insert("insert into conversation (user_low_id, user_high_id, last_seq, created_at) " +
            "values (#{userLowId}, #{userHighId}, 0, now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertConversation(ConversationEntity entity);

    @Update("update conversation set last_seq=#{lastSeq}, last_message_at=#{lastMessageAt} where id=#{id}")
    int updateConversationSummary(@Param("id") long id,
                                  @Param("lastSeq") long lastSeq,
                                  @Param("lastMessageAt") Date lastMessageAt);

    @Insert("insert into conversation_member (conversation_id, user_id, last_read_seq, created_at) " +
            "values (#{conversationId}, #{userId}, 0, now())")
    int insertMember(@Param("conversationId") long conversationId, @Param("userId") long userId);

    @Select("select conversation_id, user_id, last_read_seq, created_at from conversation_member " +
            "where conversation_id=#{conversationId} and user_id=#{userId}")
    @Results(id = "Member", value = {
            @Result(property = "conversationId", column = "conversation_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "lastReadSeq", column = "last_read_seq"),
            @Result(property = "createdAt", column = "created_at")
    })
    ConversationMemberEntity selectMember(@Param("conversationId") long conversationId, @Param("userId") long userId);

    @Select("select conversation_id, user_id, last_read_seq, created_at from conversation_member " +
            "where conversation_id=#{conversationId} and user_id=#{userId} for update")
    @ResultMap("Member")
    ConversationMemberEntity selectMemberForUpdate(@Param("conversationId") long conversationId,
                                                   @Param("userId") long userId);

    @Update("update conversation_member set last_read_seq=#{lastReadSeq} " +
            "where conversation_id=#{conversationId} and user_id=#{userId} and last_read_seq < #{lastReadSeq}")
    int advanceReadSeq(@Param("conversationId") long conversationId,
                       @Param("userId") long userId,
                       @Param("lastReadSeq") long lastReadSeq);

    @Select("<script>" +
            "select c.id, c.user_low_id, c.user_high_id, c.last_seq, c.last_message_at, c.created_at " +
            "from conversation c " +
            "inner join conversation_member m on m.conversation_id = c.id " +
            "where m.user_id = #{userId} " +
            "<if test='cursorAt != null'>" +
            "and (coalesce(c.last_message_at,c.created_at) &lt; #{cursorAt} " +
            "or (coalesce(c.last_message_at,c.created_at) = #{cursorAt} and c.id &lt; #{cursorId})) " +
            "</if>" +
            "order by coalesce(c.last_message_at,c.created_at) desc, c.id desc limit #{limit}" +
            "</script>")
    @ResultMap("Conversation")
    List<ConversationEntity> listConversations(@Param("userId") long userId,
                                               @Param("cursorAt") Date cursorAt,
                                               @Param("cursorId") Long cursorId,
                                               @Param("limit") int limit);

    @Insert("insert into chat_message (conversation_id, seq, sender_id, client_message_id, type, content, " +
            "image_key, image_content_type, image_width, image_height, image_size, image_sha256, created_at) " +
            "values (#{conversationId}, #{seq}, #{senderId}, #{clientMessageId}, #{type}, #{content}, " +
            "#{imageKey}, #{imageContentType}, #{imageWidth}, #{imageHeight}, #{imageSize}, #{imageSha256}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMessage(ChatMessageEntity entity);

    @Select("select id, conversation_id, seq, sender_id, client_message_id, type, content, " +
            "image_key, image_content_type, image_width, image_height, image_size, image_sha256, created_at " +
            "from chat_message where conversation_id=#{conversationId} and sender_id=#{senderId} " +
            "and client_message_id=#{clientMessageId} limit 1")
    // 等待账号行锁后必须重新读数据库，不能复用一级缓存中此前的空结果。
    @Options(useCache = false, flushCache = Options.FlushCachePolicy.TRUE)
    @Results(id = "ChatMessage", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "conversationId", column = "conversation_id"),
            @Result(property = "seq", column = "seq"),
            @Result(property = "senderId", column = "sender_id"),
            @Result(property = "clientMessageId", column = "client_message_id"),
            @Result(property = "type", column = "type"),
            @Result(property = "content", column = "content"),
            @Result(property = "imageKey", column = "image_key"),
            @Result(property = "imageContentType", column = "image_content_type"),
            @Result(property = "imageWidth", column = "image_width"),
            @Result(property = "imageHeight", column = "image_height"),
            @Result(property = "imageSize", column = "image_size"),
            @Result(property = "imageSha256", column = "image_sha256"),
            @Result(property = "createdAt", column = "created_at")
    })
    ChatMessageEntity selectByClientMessageId(@Param("conversationId") long conversationId,
                                              @Param("senderId") long senderId,
                                              @Param("clientMessageId") String clientMessageId);

    @Select("select id, conversation_id, seq, sender_id, client_message_id, type, content, " +
            "image_key, image_content_type, image_width, image_height, image_size, image_sha256, created_at " +
            "from chat_message where id=#{id} and conversation_id=#{conversationId} limit 1")
    @ResultMap("ChatMessage")
    ChatMessageEntity selectByIdInConversation(@Param("conversationId") long conversationId, @Param("id") long id);

    @Select("select id, conversation_id, seq, sender_id, client_message_id, type, content, " +
            "image_key, image_content_type, image_width, image_height, image_size, image_sha256, created_at " +
            "from chat_message where conversation_id=#{conversationId} and seq=#{seq} limit 1")
    @ResultMap("ChatMessage")
    ChatMessageEntity selectBySeq(@Param("conversationId") long conversationId, @Param("seq") long seq);

    @Select("select id, conversation_id, seq, sender_id, client_message_id, type, content, " +
            "image_key, image_content_type, image_width, image_height, image_size, image_sha256, created_at " +
            "from chat_message where conversation_id=#{conversationId} order by seq desc limit 1")
    @ResultMap("ChatMessage")
    ChatMessageEntity selectLastMessage(@Param("conversationId") long conversationId);

    @Select("<script>" +
            "select id, conversation_id, seq, sender_id, client_message_id, type, content, " +
            "image_key, image_content_type, image_width, image_height, image_size, image_sha256, created_at " +
            "from chat_message where conversation_id=#{conversationId} " +
            "<if test='beforeSeq != null'>and seq &lt; #{beforeSeq} order by seq desc</if>" +
            "<if test='afterSeq != null'>and seq &gt; #{afterSeq} order by seq asc</if>" +
            "<if test='beforeSeq == null and afterSeq == null'>order by seq desc</if>" +
            " limit #{limit}" +
            "</script>")
    @ResultMap("ChatMessage")
    List<ChatMessageEntity> selectMessages(@Param("conversationId") long conversationId,
                                           @Param("beforeSeq") Long beforeSeq,
                                           @Param("afterSeq") Long afterSeq,
                                           @Param("limit") int limit);

    @Select("select count(*) from chat_message " +
            "where conversation_id=#{conversationId} and sender_id=#{peerId} and seq > #{lastReadSeq}")
    int countUnreadFromPeer(@Param("conversationId") long conversationId,
                            @Param("peerId") long peerId,
                            @Param("lastReadSeq") long lastReadSeq);

    @Select("select coalesce(sum(cnt),0) from (" +
            "select count(*) as cnt from conversation_member m " +
            "inner join conversation c on c.id = m.conversation_id " +
            "inner join chat_message msg on msg.conversation_id = c.id " +
            "where m.user_id = #{userId} and msg.sender_id != #{userId} and msg.seq > m.last_read_seq " +
            "group by c.id) t")
    int countTotalUnread(@Param("userId") long userId);
}
