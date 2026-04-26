package cn.gdeiassistant.core.campuscredential.controller;

import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.campuscredential.pojo.dto.CampusCredentialConsentDTO;
import cn.gdeiassistant.core.campuscredential.pojo.dto.CampusCredentialQuickAuthUpdateDTO;
import cn.gdeiassistant.core.campuscredential.pojo.vo.CampusCredentialStatusVO;
import cn.gdeiassistant.core.campuscredential.service.CampusCredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/campus-credential")
public class CampusCredentialController {

    @Autowired
    private CampusCredentialService campusCredentialService;

    @GetMapping("/status")
    public DataJsonResult<CampusCredentialStatusVO> getStatus(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true, campusCredentialService.getSelfStatus(sessionId));
    }

    @PostMapping("/consent")
    public DataJsonResult<CampusCredentialStatusVO> recordConsent(HttpServletRequest request,
                                                                  @RequestBody(required = false) CampusCredentialConsentDTO body) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        CampusCredentialConsentDTO dto = body != null ? body : new CampusCredentialConsentDTO();
        dto.setScene(CampusCredentialService.SCENE_SETTINGS);
        dto.setPolicyDate(CampusCredentialService.DEFAULT_POLICY_DATE);
        dto.setEffectiveDate(CampusCredentialService.DEFAULT_EFFECTIVE_DATE);
        return new DataJsonResult<>(true, campusCredentialService.recordConsent(sessionId, dto));
    }

    @PostMapping("/revoke")
    public DataJsonResult<CampusCredentialStatusVO> revokeConsent(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true, campusCredentialService.revokeConsent(sessionId));
    }

    @DeleteMapping
    public DataJsonResult<CampusCredentialStatusVO> deleteSavedCredential(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true, campusCredentialService.deleteSavedCredential(sessionId));
    }

    @PostMapping("/quick-auth")
    public DataJsonResult<CampusCredentialStatusVO> updateQuickAuth(HttpServletRequest request,
                                                                    @RequestBody CampusCredentialQuickAuthUpdateDTO body) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        return new DataJsonResult<>(true, campusCredentialService.updateQuickAuth(sessionId, body.isEnabled()));
    }
}
