package cn.gdeiassistant.core.message.controller;

import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.common.tools.Utils.PageUtils;
import cn.gdeiassistant.core.i18n.ApiLanguageResolver;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.message.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/information/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/interaction/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<InteractionMessageVO>> getInteractionMessages(HttpServletRequest request,
            @PathVariable("start") Integer start,
            @PathVariable("size") Integer size) {
        size = PageUtils.normalizePageSize(start, size);
        String sessionId = (String) request.getAttribute("sessionId");
        String language = ApiLanguageResolver.normalizeLanguage(request.getHeader("Accept-Language"));
        List<InteractionMessageVO> messages = messageService.queryInteractionMessages(sessionId, start, size);
        if (messages == null || messages.isEmpty() || ApiLanguageResolver.isSimplifiedChinese(language)) {
            return new DataJsonResult<>(true, messages);
        }
        List<InteractionMessageVO> localizedMessages = new ArrayList<>(messages.size());
        for (InteractionMessageVO message : messages) {
            localizedMessages.add(BackendTextLocalizer.localizeInteractionMessage(message, language));
        }
        return new DataJsonResult<>(true, localizedMessages);
    }

    @RequestMapping(value = "/unread", method = RequestMethod.GET)
    public DataJsonResult<Integer> getInteractionUnreadCount(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true, messageService.queryInteractionUnreadCount(sessionId));
    }

    @RequestMapping(value = "/id/{id}/read", method = RequestMethod.POST)
    public JsonResult readInteractionMessage(HttpServletRequest request, @PathVariable("id") String id) {
        String sessionId = (String) request.getAttribute("sessionId");
        messageService.markInteractionMessageRead(sessionId, id);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/readall", method = RequestMethod.POST)
    public JsonResult readAllInteractionMessages(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        messageService.markAllInteractionMessagesRead(sessionId);
        return new JsonResult(true);
    }
}
