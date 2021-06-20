package cn.gdeiassistant.Controller.Account.RestController;

import cn.gdeiassistant.Annotation.UserGroupAccess;
import cn.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.Account.PasswordService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class PasswordRestController {

    @Autowired
    private PasswordService passwordService;

    /**
     * 修改用户账号
     *
     * @param request
     * @param oldPassword
     * @param newPassword
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/password", method = RequestMethod.POST)
    @UserGroupAccess(group = {1, 2, 3, 4, 5, 6}, rest = true)
    public JsonResult ChangePassword(HttpServletRequest request, HttpServletResponse response
            , @Validated @NotBlank @Length(max = 35) String oldPassword
            , @Validated @NotBlank @Length(max = 35) String newPassword) throws Exception {
        if (oldPassword.equals(newPassword)) {
            return new JsonResult(false, "新密码不能与旧密码相同");
        }
        String username = (String) request.getSession().getAttribute("username");
        Integer group = (Integer) request.getSession().getAttribute("group");
        if (group.equals(UserGroupEnum.STUDENT.getValue()) || group.equals(UserGroupEnum.TEACHER.getValue())) {
            //学生和教师用户不允许通过应用修改密码
            return new JsonResult(false, "请登录学院统一认证平台修改密码并重新登录应用进行同步");
        }
        passwordService.ChangeUserPassword(username, oldPassword, newPassword);
        //更新Session
        request.getSession().setAttribute("password", newPassword);
        //更新Cookie
        Cookie usernameCookie = new Cookie("username", username);
        Cookie passwordCookie = new Cookie("password", newPassword);
        //设置Cookie最大有效时间为3个月
        usernameCookie.setMaxAge(7776000);
        usernameCookie.setPath("/");
        usernameCookie.setHttpOnly(true);
        passwordCookie.setMaxAge(7776000);
        passwordCookie.setPath("/");
        passwordCookie.setHttpOnly(true);
        response.addCookie(usernameCookie);
        response.addCookie(passwordCookie);
        return new JsonResult(true);
    }

}
