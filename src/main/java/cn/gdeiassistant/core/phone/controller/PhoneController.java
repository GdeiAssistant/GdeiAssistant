package cn.gdeiassistant.core.phone.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.exception.VerificationException.SendSMSException;
import cn.gdeiassistant.common.exception.VerificationException.VerificationCodeInvalidException;
import cn.gdeiassistant.common.pojo.Entity.Attribution;
import cn.gdeiassistant.core.phone.pojo.dto.PhoneBindDTO;
import cn.gdeiassistant.core.phone.pojo.vo.PhoneVO;
import cn.gdeiassistant.core.profile.pojo.AttributionComparator;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.phone.service.PhoneService;
import cn.gdeiassistant.common.tools.Utils.LocationUtils;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class PhoneController {

    @Autowired
    private PhoneService phoneService;

    private JsonResult failure(HttpServletRequest request, String message) {
        return new JsonResult(false, BackendTextLocalizer.localizeMessage(message, request != null ? request.getHeader("Accept-Language") : null));
    }

    /**
     * 获取国际手机区号表
     *
     * @return
     */
    @RequestMapping(value = "/api/phone/attribution", method = RequestMethod.GET)
    public DataJsonResult<List<Attribution>> queryPhoneAttributionMap() {
        Map<Integer, Attribution> attributionMap = LocationUtils.getAttributionMap();
        //根据国家/地区首字拼音转换为有序数组列表
        List<Attribution> list = new ArrayList<>(attributionMap.values());
        list.sort(new AttributionComparator());
        return new DataJsonResult<>(true, list);
    }

    /**
     * 获取手机号验证码
     *
     * @param request
     * @param phone
     * @return
     */
    @RateLimit(maxRequests = 3, windowSeconds = 60)
    @RequestMapping(value = "/api/phone/verification", method = RequestMethod.POST)
    public JsonResult GetPhoneVerificationCode(HttpServletRequest request, @Validated @NotNull @Min(0) @Max(999) Integer code
            , @Validated @NotBlank @Length(min = 7, max = 11) @Pattern(regexp = "^[0-9]*$") String phone) throws SendSMSException {
        if (LocationUtils.getAttributionMap().get(code) == null) {
            return failure(request, "不受支持的国际手机区号");
        }
        phoneService.getPhoneVerificationCode(code, phone);
        return new JsonResult(true);
    }

    /**
     * 绑定手机号
     *
     * @param request
     * @param code
     * @param phone
     * @return
     */
    @RequestMapping(value = "/api/phone/attach", method = RequestMethod.POST)
    public JsonResult attachPhoneNumber(HttpServletRequest request, @Validated @NotNull @Min(0) @Max(999) Integer code
            , @Validated @NotBlank @Length(min = 7, max = 11) @Pattern(regexp = "^[0-9]*$") String phone
            , @Validated @NotNull @Min(10000) @Max(999999) Integer randomCode) throws VerificationCodeInvalidException {
        if (LocationUtils.getAttributionMap().get(code) == null) {
            return failure(request, "不受支持的国际手机区号");
        }
        phoneService.checkVerificationCode(code, phone, randomCode);
        String sessionId = (String) request.getAttribute("sessionId");
        PhoneBindDTO dto = new PhoneBindDTO();
        dto.setCode(code);
        dto.setPhone(phone);
        phoneService.attachUserPhone(sessionId, dto);
        return new JsonResult(true);
    }

    /**
     * 解绑手机号
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/phone/unattach", method = RequestMethod.POST)
    public JsonResult unAttachPhoneNumber(HttpServletRequest request)  {
        String sessionId = (String) request.getAttribute("sessionId");
        PhoneVO phone = phoneService.queryUserPhone(sessionId);
        if (phone != null) {
            phoneService.unAttachUserPhone(sessionId);
            return new JsonResult(true);
        }
        return failure(request, "当前用户未绑定手机号");
    }

    /**
     * 查询当前用户手机号绑定状态
     * GET /api/phone/status
     */
    @RequestMapping(value = "/api/phone/status", method = RequestMethod.GET)
    public DataJsonResult<PhoneVO> queryPhoneStatus(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        PhoneVO phone = phoneService.queryUserPhone(sessionId);
        return new DataJsonResult<>(true, phone);
    }

}
