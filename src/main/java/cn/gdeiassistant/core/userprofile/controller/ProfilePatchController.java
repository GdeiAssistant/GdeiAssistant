package cn.gdeiassistant.core.userprofile.controller;

import cn.gdeiassistant.common.pojo.result.JsonResult;
import cn.gdeiassistant.core.profile.service.ProfilePatchService;
import cn.gdeiassistant.core.userprofile.controller.request.ProfilePatchRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class ProfilePatchController {
    private final ProfilePatchService service;
    public ProfilePatchController(ProfilePatchService service) { this.service = service; }
    public static class ValidationResult extends JsonResult {
        private final Map<String, String> errors;
        public ValidationResult(Map<String, String> errors) { super(false, "请检查个人资料"); this.errors = errors; }
        public Map<String, String> getErrors() { return errors; }
    }
    /**
     * 局部更新个人资料。仅提交需要修改的字段，未提交字段保持不变。
     */
    @RequestMapping(value = "/api/profile", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<? extends JsonResult> save(HttpServletRequest request, @RequestBody ProfilePatchRequest patch) throws Exception {
        try {
            service.save((String) request.getAttribute("sessionId"), patch == null ? null : patch.toPatchMap());
            return ResponseEntity.ok(new JsonResult(true));
        } catch (ProfilePatchService.InvalidPatch e) {
            return ResponseEntity.badRequest().body(new ValidationResult(e.getErrors()));
        }
    }
}
