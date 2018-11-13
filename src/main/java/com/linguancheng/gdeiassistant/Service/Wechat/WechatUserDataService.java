package com.linguancheng.gdeiassistant.Service.Wechat;

import com.linguancheng.gdeiassistant.Enum.Base.AttachResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.WechatUser.WechatUserMapper;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class WechatUserDataService {

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Resource(name = "wechatUserMapper")
    private WechatUserMapper wechatUserMapper;

    /**
     * 检查微信账号绑定教务系统账号
     *
     * @param wechatID
     * @return
     */
    public BaseResult<String, AttachResultEnum> CheckWechatAttachState(String wechatID) {
        BaseResult<String, AttachResultEnum> checkWechatAttachStateResult = new BaseResult<>();
        try {
            String username = wechatUserMapper.selectUsername(wechatID);
            if (username != null && !username.trim().isEmpty()) {
                checkWechatAttachStateResult.setResultData(StringEncryptUtils.decryptString(username));
                checkWechatAttachStateResult.setResultType(AttachResultEnum.ATTACHED);
                return checkWechatAttachStateResult;
            }
            checkWechatAttachStateResult.setResultType(AttachResultEnum.NOT_ATTACHED);
        } catch (Exception e) {
            e.printStackTrace();
            checkWechatAttachStateResult.setResultType(AttachResultEnum.SERVER_ERROR);
        }
        return checkWechatAttachStateResult;
    }

    /**
     * 通过WechatID对应的Username查询用户信息数据
     *
     * @param username
     * @return
     */
    public BaseResult<User, BoolResultEnum> QueryWechatUserData(String username) {
        BaseResult<User, BoolResultEnum> result = new BaseResult<>();
        try {
            User queryUser = userMapper.selectUser(StringEncryptUtils.encryptString(username)).decryptUser();
            if (queryUser != null) {
                result.setResultData(queryUser);
                result.setResultType(BoolResultEnum.SUCCESS);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setResultType(BoolResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 同步微信账号与教务系统账号绑定的数据
     *
     * @param username
     * @param wechatUserID
     * @return
     */
    public void SyncWechatUserData(String username, String wechatUserID) throws Exception {
        if (wechatUserMapper.selectUsername(wechatUserID) != null) {
            //更新微信账号绑定的教务系统账号
            wechatUserMapper.updateWechatUser(wechatUserID, StringEncryptUtils.encryptString(username));
        } else {
            wechatUserMapper.insertWechatUser(wechatUserID, StringEncryptUtils.encryptString(username));
        }
    }
}
