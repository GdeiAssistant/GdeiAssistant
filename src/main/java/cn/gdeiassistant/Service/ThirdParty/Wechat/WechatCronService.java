package cn.gdeiassistant.Service.ThirdParty.Wechat;

import cn.gdeiassistant.Pojo.Entity.Reading;
import cn.gdeiassistant.Pojo.Entity.WechatAccount;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantData.Reading.ReadingMapper;
import cn.gdeiassistant.Tools.Utils.WechatAccountUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
@Profile("production")
public class WechatCronService {

    private Logger logger = LoggerFactory.getLogger(WechatCronService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private WechatAccountService wechatAccountService;

    @Autowired
    private ReadingMapper readingMapper;

    /**
     * 定时更新微信公众号数据
     */
    @Scheduled(fixedDelay = 21600000)
    public void UpdateAccountData() {
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
     * 定时同步微信专题阅读素材
     *
     * @return
     */
    @Scheduled(fixedDelay = 21600000)
    @Transactional("dataTransactionManager")
    public void SyncWechatReadingItem() {
        logger.info("{}启动了同步微信专题阅读素材的任务", LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
        String accessToken = wechatService.GetWechatAccessToken();
        //获取专题阅读素材总数
        JSONObject jsonObject = restTemplate.getForObject("https://api.weixin.qq.com/cgi-bin/material/get_materialcount?access_token=" + accessToken, JSONObject.class);
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
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            JSONObject result = restTemplate.postForObject("https://api.weixin.qq.com/cgi-bin/material/batchget_material?access_token=" + accessToken
                    , new HttpEntity<>(params, httpHeaders), JSONObject.class);
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
                    if (readingMapper.selectReadingById(items.getJSONObject(j).getString("media_id")) == null) {
                        readingMapper.insertReading(reading);
                    } else {
                        readingMapper.updateReading(reading);
                    }
                }
            }
        }
    }
}
