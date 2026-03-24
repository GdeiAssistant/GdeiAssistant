package cn.gdeiassistant.core.evaluate.controller;

import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.evaluate.pojo.dto.EvaluateSubmitDTO;
import cn.gdeiassistant.core.evaluate.service.EvaluateService;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class EvaluateController {

    @Autowired
    private EvaluateService evaluateService;

    /**
     * 教学评价提交。统一走 /api/evaluate/submit，从请求头 token 鉴权，返回 JSON。
     * 执行教学评价提交。
     */
    @RequestMapping(value = "/api/evaluate/submit", method = RequestMethod.POST)
    public JsonResult submit(HttpServletRequest request,
                             @RequestBody(required = false) EvaluateSubmitDTO body) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        if (StringUtils.isBlank(sessionId)) {
            return new JsonResult(false, "未检测到有效令牌");
        }
        boolean directlySubmit = body != null && Boolean.TRUE.equals(body.getDirectSubmit());
        evaluateService.TeacherEvaluate(sessionId, directlySubmit);
        return new JsonResult(true);
    }
}
