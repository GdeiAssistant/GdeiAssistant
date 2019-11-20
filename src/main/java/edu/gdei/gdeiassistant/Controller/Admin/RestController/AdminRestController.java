package edu.gdei.gdeiassistant.Controller.Admin.RestController;

import edu.gdei.gdeiassistant.Annotation.UserGroupAccess;
import edu.gdei.gdeiassistant.Pojo.Entity.Admin;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
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
        admin.setPridetheme(Boolean.TRUE.equals(servletContext.getAttribute("pridetheme")));
        admin.setAuthenticationForce(Boolean.TRUE.equals(servletContext.getAttribute("authentication.force")));
        admin.setAuthenticationErshou(Boolean.TRUE.equals(servletContext.getAttribute("authentication.ershou")));
        admin.setAuthenticationLostAndFound(Boolean.TRUE.equals(servletContext.getAttribute("authentication.lostandfound")));
        admin.setAuthenticationSecret(Boolean.TRUE.equals(servletContext.getAttribute("authentication.secret")));
        admin.setAuthenticationDelivery(Boolean.TRUE.equals(servletContext.getAttribute("authentication.delivery")));
        admin.setAuthenticationPhotograph(Boolean.TRUE.equals(servletContext.getAttribute("authentication.photograph")));
        admin.setAuthenticationExpress(Boolean.TRUE.equals(servletContext.getAttribute("authentication.express")));
        return new DataJsonResult<>(true, admin);
    }

    @RequestMapping(value = "/api/admin", method = RequestMethod.POST)
    @UserGroupAccess(group = {1}, rest = true)
    public JsonResult UpdateAdminSetting(HttpServletRequest request, int index, boolean state) throws Exception {
        switch (index) {
            case 0:
                //黑白网页
                servletContext.setAttribute("grayscale", state);
                break;

            case 1:
                //Pride主题
                servletContext.setAttribute("pridetheme", state);
                break;

            case 2:
                //强制实名认证
                servletContext.setAttribute("authentication.force", state);
                break;

            case 3:
                //二手交易实名认证
                servletContext.setAttribute("authentication.ershou", state);
                break;

            case 4:
                //失物招领实名认证
                servletContext.setAttribute("authentication.lostandfound", true);
                break;

            case 5:
                //校园树洞实名认证
                servletContext.setAttribute("authentication.secret", true);
                break;

            case 6:
                //全民快递实名认证
                servletContext.setAttribute("authentication.delivery", true);
                break;

            case 7:
                //拍好校园实名认证
                servletContext.setAttribute("authentication.photograph", true);
                break;

            case 8:
                //表白墙实名认证
                servletContext.setAttribute("authentication.express", true);
                break;

            default:
                return new JsonResult(false, "请求参数不合法");
        }
        return new JsonResult(true);
    }
}
