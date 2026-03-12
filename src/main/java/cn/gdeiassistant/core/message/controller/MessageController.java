package cn.gdeiassistant.core.message.controller;

import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.message.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MessageController {

    @Autowired
    private MessageService messageService;

    @RequestMapping(value = "/api/message/interaction/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<InteractionMessageVO>> getInteractionMessages(HttpServletRequest request,
            @PathVariable("start") Integer start,
            @PathVariable("size") Integer size,
            @RequestParam(value = "includeLegacyDating", defaultValue = "false") Boolean includeLegacyDating) {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true,
                messageService.queryInteractionMessages(sessionId, start, size, Boolean.TRUE.equals(includeLegacyDating)));
    }

    @RequestMapping(value = "/api/message/unread", method = RequestMethod.GET)
    public DataJsonResult<Integer> getInteractionUnreadCount(HttpServletRequest request,
            @RequestParam(value = "includeLegacyDating", defaultValue = "false") Boolean includeLegacyDating) {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true,
                messageService.queryInteractionUnreadCount(sessionId, Boolean.TRUE.equals(includeLegacyDating)));
    }

    @RequestMapping(value = "/api/message/id/{id}/read", method = RequestMethod.POST)
    public JsonResult readInteractionMessage(HttpServletRequest request, @PathVariable("id") String id) {
        String sessionId = (String) request.getAttribute("sessionId");
        messageService.markInteractionMessageRead(sessionId, id);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/message/readall", method = RequestMethod.POST)
    public JsonResult readAllInteractionMessages(HttpServletRequest request,
            @RequestParam(value = "includeLegacyDating", defaultValue = "false") Boolean includeLegacyDating) {
        String sessionId = (String) request.getAttribute("sessionId");
        messageService.markAllInteractionMessagesRead(sessionId, Boolean.TRUE.equals(includeLegacyDating));
        return new JsonResult(true);
    }
}
