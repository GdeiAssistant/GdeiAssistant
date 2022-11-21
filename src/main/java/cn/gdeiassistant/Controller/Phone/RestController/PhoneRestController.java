package cn.gdeiassistant.Controller.Phone.RestController;

import cn.gdeiassistant.Exception.VerificationException.SendSMSException;
import cn.gdeiassistant.Exception.VerificationException.VerificationCodeInvalidException;
import cn.gdeiassistant.Pojo.Entity.Attribution;
import cn.gdeiassistant.Pojo.Entity.Phone;
import cn.gdeiassistant.Pojo.Profile.AttributionComparator;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.Phone.PhoneService;
import cn.gdeiassistant.Tools.Utils.LocationUtils;
import com.aliyuncs.exceptions.ClientException;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class PhoneRestController {

    @Autowired
    private PhoneService phoneService;

    /**
     * 获取国际手机区号表
     *
     * @return
     */
    @RequestMapping(value = "/rest/phone/attribution", method = RequestMethod.GET)
    public DataJsonResult<List<Attribution>> QueryPhoneAttributionMap() {
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
    @RequestMapping(value = "/api/phone/verification", method = RequestMethod.POST)
    public JsonResult GetPhoneVerificationCode(HttpServletRequest request, @Validated @NotNull @Min(0) @Max(999) Integer code
            , @Validated @NotBlank @Length(min = 7, max = 11) @Pattern(regexp = "^[0-9]*$") String phone) throws ClientException, SendSMSException {
        if (LocationUtils.getAttributionMap().get(code) == null) {
            return new JsonResult(false, "不受支持的国际手机区号");
        }
        phoneService.GetPhoneVerificationCode(code, phone);
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
    public JsonResult AttachPhoneNumber(HttpServletRequest request, @Validated @NotNull @Min(0) @Max(999) Integer code
            , @Validated @NotBlank @Length(min = 7, max = 11) @Pattern(regexp = "^[0-9]*$") String phone
            , @Validated @NotNull @Min(10000) @Max(999999) Integer randomCode) throws VerificationCodeInvalidException {
        if (LocationUtils.getAttributionMap().get(code) == null) {
            return new JsonResult(false, "不受支持的国际手机区号");
        }
        phoneService.CheckVerificationCode(code, phone, randomCode);
        phoneService.AttachUserPhone(request.getSession().getId(), code, phone);
        return new JsonResult(true);
    }

    /**
     * 解绑手机号
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/phone/unattach", method = RequestMethod.POST)
    public JsonResult UnAttachPhoneNumber(HttpServletRequest request)  {
        Phone phone = phoneService.QueryUserPhone(request.getSession().getId());
        if (phone != null) {
            phoneService.UnAttachUserPhone(request.getSession().getId());
            return new JsonResult(true);
        }
        return new JsonResult(false, "当前用户未绑定手机号");
    }

}
