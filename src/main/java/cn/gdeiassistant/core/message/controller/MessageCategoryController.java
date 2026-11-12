package cn.gdeiassistant.core.message.controller;

import cn.gdeiassistant.common.pojo.result.*;
import cn.gdeiassistant.common.tools.utils.PageUtils;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.message.service.InteractionMessageLocalizer;
import cn.gdeiassistant.common.i18n.ApiLanguageResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/information/message")
public class MessageCategoryController {
    private final InteractionNotificationService service;
    public MessageCategoryController(InteractionNotificationService service) { this.service = service; }
    @GetMapping("/categories/unread")
    public DataJsonResult<Map<String, Integer>> unread(HttpServletRequest request) throws Exception {
        return new DataJsonResult<>(true, service.categoryUnread((String) request.getAttribute("sessionId")));
    }
    @GetMapping({"/service/start/{start}/size/{size}", "/community/start/{start}/size/{size}"})
    public DataJsonResult<List<InteractionMessageVO>> list(HttpServletRequest request, @PathVariable int start, @PathVariable int size) throws Exception {
        size = PageUtils.normalizePageSize(start, size);
        boolean delivery = request.getRequestURI().contains("/message/service/");
        String language = ApiLanguageResolver.normalizeLanguage(request.getHeader("Accept-Language"));
        List<InteractionMessageVO> messages = service.queryCategory((String) request.getAttribute("sessionId"), delivery, start, size);
        return new DataJsonResult<>(true, messages.stream().map(item -> InteractionMessageLocalizer.localize(item, language)).toList());
    }
    @PostMapping({"/service/readall", "/community/readall"})
    public JsonResult readAll(HttpServletRequest request) throws Exception {
        service.markCategoryRead((String) request.getAttribute("sessionId"), request.getRequestURI().contains("/message/service/"));
        return new JsonResult(true);
    }
}
