package cn.gdeiassistant.Controller.Admin.RestController;

import cn.gdeiassistant.Annotation.UserGroupAccess;
import cn.gdeiassistant.Pojo.Entity.Admin;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

@RestController
public class AdminRestController {

    @Autowired
    private ServletContext servletContext;

    @RequestMapping(value = "/api/admin", method = RequestMethod.GET)
    @UserGroupAccess(group = {1}, rest = true)
    public DataJsonResult<Admin> QueryAdminSetting(HttpServletRequest request) {
        Admin admin = new Admin();
        admin.setGrayscale(Boolean.TRUE.equals(servletContext.getAttribute("grayscale")));
        admin.setPrideThemeLogo(Boolean.TRUE.equals(servletContext.getAttribute("pridethemelogo")));
        admin.setPinkThemeLogo(Boolean.TRUE.equals(servletContext.getAttribute("pinkthemelogo")));
        admin.setAuthenticationForce(Boolean.TRUE.equals(servletContext.getAttribute("authentication.force")));
        admin.setAuthenticationErshou(Boolean.TRUE.equals(servletContext.getAttribute("authentication.ershou")));
        admin.setAuthenticationLostAndFound(Boolean.TRUE.equals(servletContext.getAttribute("authentication.lostandfound")));
        admin.setAuthenticationSecret(Boolean.TRUE.equals(servletContext.getAttribute("authentication.secret")));
        admin.setAuthenticationDelivery(Boolean.TRUE.equals(servletContext.getAttribute("authentication.delivery")));
        admin.setAuthenticationPhotograph(Boolean.TRUE.equals(servletContext.getAttribute("authentication.photograph")));
        admin.setAuthenticationExpress(Boolean.TRUE.equals(servletContext.getAttribute("authentication.express")));
        admin.setAuthenticationTopic(Boolean.TRUE.equals(servletContext.getAttribute("authentication.topic")));
        return new DataJsonResult<>(true, admin);
    }

    @RequestMapping(value = "/api/admin", method = RequestMethod.POST)
    @UserGroupAccess(group = {1}, rest = true)
    public JsonResult UpdateAdminSetting(HttpServletRequest request, String index, boolean state) throws Exception {
        switch (index.toUpperCase()) {
            case "GRAYSCALE":
                //黑白网页
                servletContext.setAttribute("grayscale", state);
                break;

            case "PRIDETHEMELOGO":
                //Pride主题Logo
                servletContext.setAttribute("pridethemelogo", state);
                break;

            case "PINKTHEMELOGO":
                //Pink主题Logo
                servletContext.setAttribute("pinkthemelogo", state);
                break;

            case "FORCE_AUTHENTICATION":
                //强制实名认证
                servletContext.setAttribute("authentication.force", state);
                break;

            case "ERSHOU_AUTHENTICATION":
                //二手交易实名认证
                servletContext.setAttribute("authentication.ershou", state);
                break;

            case "LOSTANDFOUND_AUTHENTICATION":
                //失物招领实名认证
                servletContext.setAttribute("authentication.lostandfound", true);
                break;

            case "SECRET_AUTHENTICATION":
                //校园树洞实名认证
                servletContext.setAttribute("authentication.secret", true);
                break;

            case "DELIVERY_AUTHENTICATION":
                //全民快递实名认证
                servletContext.setAttribute("authentication.delivery", true);
                break;

            case "PHOTOGRAPH_AUTHENTICATION":
                //拍好校园实名认证
                servletContext.setAttribute("authentication.photograph", true);
                break;

            case "EXPRESS_AUTHENTICATION":
                //表白墙实名认证
                servletContext.setAttribute("authentication.express", true);
                break;

            case "TOPIC_AUTHENTICATION":
                //话题实名认证
                servletContext.setAttribute("authentication.topic", true);
                break;

            default:
                return new JsonResult(false, "请求参数不合法");
        }
        return new JsonResult(true);
    }
}
