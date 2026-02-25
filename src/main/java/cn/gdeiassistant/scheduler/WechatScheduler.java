package cn.gdeiassistant.scheduler;

import cn.gdeiassistant.core.wechat.service.WechatCronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 微信相关定时触发：仅负责按固定间隔调用 WechatCronService 的业务方法，不包含业务逻辑。
 */
@Component
@Profile("production")
public class WechatScheduler {

    @Autowired(required = false)
    private WechatCronService wechatCronService;

    @Scheduled(fixedDelay = 21600000)
    public void updateAccountData() {
        if (wechatCronService != null) {
            wechatCronService.updateAccountData();
        }
    }

    @Scheduled(fixedDelay = 21600000)
    public void syncWechatReadingItem() {
        if (wechatCronService != null) {
            wechatCronService.syncWechatReadingItem();
        }
    }
}
