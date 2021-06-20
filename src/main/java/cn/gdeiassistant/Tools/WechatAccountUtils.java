package cn.gdeiassistant.Tools;

import cn.gdeiassistant.Pojo.Entity.WechatAccount;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;

public class WechatAccountUtils {

    private static List<WechatAccount> wechatAccountList = new ArrayList<>();

    static {
        //加载校园公众号列表
        LoadSchoolWechatAccountData();
    }

    public static List<WechatAccount> getWechatAccountList() {
        return wechatAccountList;
    }

    private static void LoadSchoolWechatAccountData() {
        Resource resource = new ClassPathResource("/config/info/account.xml");
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(resource.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Element root = document.getRootElement();
        List<Element> accountElements = root.elements();
        for (Element accountElement : accountElements) {
            String id = accountElement.attributeValue("Id");
            String biz = accountElement.attributeValue("Biz");
            String name = accountElement.attributeValue("Name");
            String description = accountElement.attributeValue("Description");
            String avatar = accountElement.attributeValue("Avatar");
            WechatAccount wechatAccount = new WechatAccount();
            wechatAccount.setId(id);
            wechatAccount.setBiz(biz);
            wechatAccount.setName(name);
            wechatAccount.setDescription(description);
            wechatAccount.setAvatar(avatar);
            WechatAccountUtils.wechatAccountList.add(wechatAccount);
        }
    }

}
