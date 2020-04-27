package edu.gdei.gdeiassistant.Service.YiBan;

import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.YiBanUser.YiBanUserMapper;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class YiBanUserDataService {

    @Resource(name = "yiBanUserMapper")
    private YiBanUserMapper yiBanUserMapper;

    /**
     * 同步易班账号与教务系统账号绑定的数据
     *
     * @param username
     * @param userID
     * @return
     */
    public void SyncYiBanUserData(String username, String userID) throws Exception {
        int id = Integer.valueOf(userID);
        if (yiBanUserMapper.selectUsername(id) != null) {
            //更新易班账号绑定的教务系统账号
            yiBanUserMapper.updateYiBanUser(id, StringEncryptUtils.encryptString(username));
        } else {
            yiBanUserMapper.insertYiBanUser(id, StringEncryptUtils.encryptString(username));
        }
    }

    /**
     * 检查易班用户ID绑定的教务系统的账号用户名
     *
     * @param userID
     * @return
     */
    public String GetYiBanAttachUsername(String userID) throws Exception {
        int id = Integer.valueOf(userID);
        String encryptedUsername = yiBanUserMapper.selectUsername(id);
        if (StringUtils.isBlank(encryptedUsername)) {
            //账号未绑定教务系统
            return null;
        }
        //账号已绑定教务系统
        return StringEncryptUtils.decryptString(encryptedUsername);
    }
}
