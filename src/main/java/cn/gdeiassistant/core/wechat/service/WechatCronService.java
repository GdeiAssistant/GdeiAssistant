package cn.gdeiassistant.core.wechat.service;

import cn.gdeiassistant.common.pojo.Entity.Reading;
import cn.gdeiassistant.common.pojo.Entity.WechatAccount;
import cn.gdeiassistant.core.information.pojo.entity.ReadingEntity;
import cn.gdeiassistant.core.reading.mapper.ReadingMapper;
import cn.gdeiassistant.common.tools.Utils.WechatAccountUtils;
import cn.gdeiassistant.integration.wechat.WechatApiClient;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
@Profile("production")
public class WechatCronService {

    private final Logger logger = LoggerFactory.getLogger(WechatCronService.class);

    @Autowired
    private WechatApiClient wechatApiClient;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private WechatAccountService wechatAccountService;

    @Autowired
    private ReadingMapper readingMapper;

    /**
     * 更新微信公众号数据（可由 Scheduler 或 HTTP /cron/wechat/account 触发）。
     */
    public void updateAccountData() {
        for (int i = 0; i < WechatAccountUtils.getWechatAccountList().size(); i++) {
            WechatAccountUtils.getWechatAccountList().get(i).setIndex(i);
            try {
                WechatAccount wechatAccount = wechatAccountService.QueryLatestWechatAccountData(null
                        , WechatAccountUtils.getWechatAccountList().get(i));
                WechatAccountUtils.getWechatAccountList().set(wechatAccount.getIndex(), wechatAccount);
            } catch (Exception ignored) {

            } finally {
                try {
                    //线程休眠三秒，避免触发网络异常验证码校验
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {

                }
            }
        }
    }

    /**
     * 同步微信专题阅读素材（可由 Scheduler 或 HTTP /cron/wechat/reading 触发）。
     */
    @Transactional("dataTransactionManager")
    public void syncWechatReadingItem() {
        logger.info("{}启动了同步微信专题阅读素材的任务", LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
        String accessToken = wechatService.getWechatAccessToken();
        //获取专题阅读素材总数
        String countJson = wechatApiClient.getMaterialCountJson(accessToken);
        JSONObject jsonObject = JSONObject.parseObject(countJson);
        int count = 0;
        if (jsonObject.containsKey("news_count")) {
            count = jsonObject.getInteger("news_count");
        }
        int page = count % 20 == 0 ? (count / 20) : (count / 20 + 1);
        for (int i = 0; i < page; i++) {
            JSONObject params = new JSONObject();
            params.put("type", "news");
            params.put("offset", String.valueOf(i));
            params.put("count", "20");
            String resultJson = wechatApiClient.batchGetMaterialJson(accessToken, params);
            JSONObject result = JSONObject.parseObject(resultJson);
            if (result.containsKey("item")) {
                JSONArray items = result.getJSONArray("item");
                for (int j = 0; j < items.size(); j++) {
                    Reading reading = new Reading();
                    reading.setId(items.getJSONObject(j).getString("media_id"));
                    reading.setTitle(items.getJSONObject(j).getJSONObject("content")
                            .getJSONArray("news_item").getJSONObject(0).getString("title"));
                    reading.setDescription(items.getJSONObject(j).getJSONObject("content")
                            .getJSONArray("news_item").getJSONObject(0).getString("digest"));
                    reading.setLink(items.getJSONObject(j).getJSONObject("content")
                            .getJSONArray("news_item").getJSONObject(0).getString("url"));
                    reading.setCreateTime(Date.from(Instant.ofEpochSecond(items.getJSONObject(j).getJSONObject("content")
                            .getLongValue("create_time"))));
                    ReadingEntity readingEntity = new ReadingEntity();
                    readingEntity.setId(reading.getId());
                    readingEntity.setTitle(reading.getTitle());
                    readingEntity.setDescription(reading.getDescription());
                    readingEntity.setLink(reading.getLink());
                    readingEntity.setCreateTime(reading.getCreateTime());
                    if (readingMapper.selectReadingById(items.getJSONObject(j).getString("media_id")) == null) {
                        readingMapper.insertReading(readingEntity);
                    } else {
                        readingMapper.updateReading(readingEntity);
                    }
                }
            }
        }
    }
}
