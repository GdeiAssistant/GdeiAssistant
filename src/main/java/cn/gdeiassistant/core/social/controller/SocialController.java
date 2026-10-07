package cn.gdeiassistant.core.social.controller;

import cn.gdeiassistant.common.pojo.result.DataJsonResult;
import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import cn.gdeiassistant.core.social.exception.SocialException;
import cn.gdeiassistant.core.social.pojo.dto.ChatMessageDTO;
import cn.gdeiassistant.core.social.pojo.dto.ConversationDTO;
import cn.gdeiassistant.core.social.pojo.dto.PageDTO;
import cn.gdeiassistant.core.social.pojo.dto.SocialUserDTO;
import cn.gdeiassistant.core.social.service.SocialChatService;
import cn.gdeiassistant.core.social.service.SocialIdentityService;
import cn.gdeiassistant.core.social.service.SocialRelationService;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/social")
public class SocialController {

    @Autowired
    private SocialRelationService relationService;
    @Autowired
    private SocialChatService chatService;
    @Autowired
    private SocialIdentityService identityService;
    @Autowired
    private R2StorageService r2StorageService;

    @ExceptionHandler(SocialException.class)
    public DataJsonResult<Void> handleSocialException(SocialException ex, HttpServletResponse response, HttpServletRequest request) {
        response.setStatus(ex.getHttpStatus());
        DataJsonResult<Void> result = new DataJsonResult<>(false, null);
        result.setCode(ex.getHttpStatus());
        result.setMessage(BackendTextLocalizer.localizeMessage(ex.getMessage(), request.getHeader("Accept-Language")));
        result.setErrorCode(ex.getErrorCode());
        return result;
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    public DataJsonResult<Void> handleBadRequest(HttpServletResponse response, HttpServletRequest request) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        DataJsonResult<Void> result = new DataJsonResult<>(false, null);
        result.setCode(400);
        result.setMessage(BackendTextLocalizer.localizeMessage("请求参数不合法", request.getHeader("Accept-Language")));
        result.setErrorCode("INVALID_REQUEST");
        return result;
    }

    @GetMapping("/me")
    public DataJsonResult<SocialUserDTO> me(HttpServletRequest request) {
        return success(relationService.getMe(sessionId(request)));
    }

    @GetMapping("/users")
    public DataJsonResult<PageDTO<SocialUserDTO>> searchUsers(HttpServletRequest request,
                                                             @RequestParam(required = false) String query,
                                                             @RequestParam(required = false) String cursor,
                                                             @RequestParam(required = false) Integer limit) {
        return success(relationService.searchUsers(sessionId(request), query, cursor, limit));
    }

    @GetMapping("/users/{id}")
    public DataJsonResult<SocialUserDTO> getUser(HttpServletRequest request, @PathVariable("id") String id) {
        return success(relationService.getUser(sessionId(request), id));
    }

    @GetMapping("/users/{id}/avatar")
    public ResponseEntity<byte[]> getAvatar(HttpServletRequest request, @PathVariable("id") String id) {
        // 与公开主页同访问规则：登录 + 非拉黑 + 对方 ACTIVE
        relationService.getUser(sessionId(request), id);
        CampusAccountView target = identityService.requireActiveByPublicId(id);
        if (target.getUsername() == null) {
            return ResponseEntity.notFound().build();
        }
        try (InputStream in = r2StorageService.downloadObject(null,
                "avatar/" + target.getUsername() + ".jpg")) {
            if (in == null) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            in.transferTo(buffer);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "private, max-age=300")
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(buffer.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }

    @GetMapping("/users/{id}/relationships")
    public DataJsonResult<PageDTO<SocialUserDTO>> relationships(HttpServletRequest request,
                                                               @PathVariable("id") String id,
                                                               @RequestParam String kind,
                                                               @RequestParam(required = false) String cursor,
                                                               @RequestParam(required = false) Integer limit) {
        return success(relationService.listRelationships(sessionId(request), id, kind, cursor, limit));
    }

    @PutMapping("/users/{id}/follow")
    public DataJsonResult<SocialUserDTO> follow(HttpServletRequest request, @PathVariable("id") String id) {
        return success(relationService.follow(sessionId(request), id));
    }

    @DeleteMapping("/users/{id}/follow")
    public DataJsonResult<SocialUserDTO> unfollow(HttpServletRequest request, @PathVariable("id") String id) {
        return success(relationService.unfollow(sessionId(request), id));
    }

    @PutMapping("/users/{id}/block")
    public DataJsonResult<Map<String, Object>> block(HttpServletRequest request, @PathVariable("id") String id) {
        return success(relationService.block(sessionId(request), id));
    }

    @DeleteMapping("/users/{id}/block")
    public DataJsonResult<Map<String, Object>> unblock(HttpServletRequest request, @PathVariable("id") String id) {
        return success(relationService.unblock(sessionId(request), id));
    }

    @GetMapping("/blocks")
    public DataJsonResult<PageDTO<SocialUserDTO>> blocks(HttpServletRequest request,
                                                        @RequestParam(required = false) String cursor,
                                                        @RequestParam(required = false) Integer limit) {
        return success(relationService.listBlocks(sessionId(request), cursor, limit));
    }

    @GetMapping("/privacy")
    public DataJsonResult<Map<String, Object>> getPrivacy(HttpServletRequest request) {
        return success(relationService.getPrivacy(sessionId(request)));
    }

    @PutMapping("/privacy")
    public DataJsonResult<Map<String, Object>> putPrivacy(HttpServletRequest request,
                                                         @RequestBody Map<String, String> body) {
        return success(relationService.updatePrivacy(sessionId(request), body == null ? null : body.get("dmPolicy")));
    }

    @GetMapping("/unread")
    public DataJsonResult<Map<String, Object>> unread(HttpServletRequest request) {
        return success(chatService.unreadTotal(sessionId(request)));
    }

    @PostMapping("/conversations")
    public DataJsonResult<ConversationDTO> createConversation(HttpServletRequest request,
                                                              @RequestBody Map<String, String> body) {
        return success(chatService.createOrGetConversation(sessionId(request), body == null ? null : body.get("peerId")));
    }

    @GetMapping("/conversations")
    public DataJsonResult<PageDTO<ConversationDTO>> listConversations(HttpServletRequest request,
                                                                     @RequestParam(required = false) String cursor,
                                                                     @RequestParam(required = false) Integer limit) {
        return success(chatService.listConversations(sessionId(request), cursor, limit));
    }

    @GetMapping("/conversations/{id}")
    public DataJsonResult<ConversationDTO> getConversation(HttpServletRequest request, @PathVariable("id") String id) {
        return success(chatService.getConversation(sessionId(request), id));
    }

    @GetMapping("/conversations/{id}/messages")
    public DataJsonResult<PageDTO<ChatMessageDTO>> listMessages(HttpServletRequest request,
                                                               @PathVariable("id") String id,
                                                               @RequestParam(required = false) String beforeSeq,
                                                               @RequestParam(required = false) String afterSeq,
                                                               @RequestParam(required = false) Integer limit) {
        return success(chatService.listMessages(sessionId(request), id, beforeSeq, afterSeq, limit));
    }

    @PostMapping("/conversations/{id}/messages")
    public DataJsonResult<ChatMessageDTO> sendMessage(HttpServletRequest request,
                                                      @PathVariable("id") String id,
                                                      @RequestBody Map<String, String> body) {
        String clientMessageId = body == null ? null : body.get("clientMessageId");
        String content = body == null ? null : body.get("content");
        return success(chatService.sendMessage(sessionId(request), id, clientMessageId, content));
    }

    @PostMapping(value = "/conversations/{id}/messages/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DataJsonResult<ChatMessageDTO> sendImageMessage(HttpServletRequest request,
                                                           @PathVariable("id") String id,
                                                           @RequestParam("clientMessageId") String clientMessageId,
                                                           @RequestParam("image") org.springframework.web.multipart.MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw SocialException.invalidRequest("image 必填");
        }
        if (image.getSize() > cn.gdeiassistant.core.social.image.ChatImageCodec.MAX_BYTES) {
            throw SocialException.invalidRequest("图片不能超过 5 MiB");
        }
        byte[] bytes;
        try {
            bytes = image.getBytes();
        } catch (Exception ex) {
            throw SocialException.invalidRequest("无法读取图片");
        }
        return success(chatService.sendImageMessage(sessionId(request), id, clientMessageId, bytes, image.getContentType()));
    }

    @GetMapping("/conversations/{id}/messages/{messageId}/image")
    public ResponseEntity<byte[]> getMessageImage(HttpServletRequest request,
                                                  @PathVariable("id") String id,
                                                  @PathVariable("messageId") String messageId) {
        SocialChatService.ImageDownload download = chatService.downloadImage(sessionId(request), id, messageId);
        if (download == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(download.contentType))
                .body(download.bytes);
    }

    @PutMapping("/conversations/{id}/read")
    public DataJsonResult<Map<String, Object>> markRead(HttpServletRequest request,
                                                       @PathVariable("id") String id,
                                                       @RequestBody Map<String, String> body) {
        return success(chatService.markRead(sessionId(request), id, body == null ? null : body.get("lastReadSeq")));
    }

    private String sessionId(HttpServletRequest request) {
        Object value = request.getAttribute("sessionId");
        return value == null ? null : String.valueOf(value);
    }

    private <T> DataJsonResult<T> success(T data) {
        DataJsonResult<T> result = new DataJsonResult<>(true, data);
        result.setCode(200);
        result.setMessage("success");
        return result;
    }
}
