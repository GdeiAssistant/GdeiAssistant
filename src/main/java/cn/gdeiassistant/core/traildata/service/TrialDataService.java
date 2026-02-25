package cn.gdeiassistant.core.trailData.service;

import cn.gdeiassistant.core.cardquery.pojo.CardQuery;
import cn.gdeiassistant.common.pojo.Document.CustomScheduleDocument;
import cn.gdeiassistant.common.pojo.Document.TrialDocument;
import cn.gdeiassistant.common.pojo.Entity.Grade;
import cn.gdeiassistant.common.pojo.Entity.Schedule;
import cn.gdeiassistant.core.gradequery.pojo.GradeQueryResult;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.schedulequery.pojo.ScheduleQueryResult;
import cn.gdeiassistant.core.trial.repository.TrialDao;
import cn.gdeiassistant.core.schedulequery.service.ScheduleService;
import cn.gdeiassistant.common.tools.Utils.ScheduleUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.common.tools.Utils.WeekUtils;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class TrialDataService {

    @Autowired
    private TrialDao trialDao;

    /**
     * 加载模拟结果数据（不做模块级业务加工）。
     *
     * @param module            模块标识，对应 @TrialData.value()
     * @param isBaseJsonResult  是否为基础 JsonResult 类型
     * @param requestTime       可选的请求时间字段名
     * @param requestTimeValue  可选的请求时间字段值
     */
    public DataJsonResult loadTrialData(String module, boolean isBaseJsonResult,
                                        String requestTime, int requestTimeValue) {
        return LoadInitialTrialData(module, isBaseJsonResult, requestTime, requestTimeValue);
    }

    /**
     * 加载初始模拟数据
     *
     * @param module
     * @param isBaseJsonResult
     * @param requestTime
     * @param requestTimeValue
     * @return
     */
    private DataJsonResult LoadInitialTrialData(String module, boolean isBaseJsonResult, String requestTime
            , int requestTimeValue) {
        //基础类型返回值
        if (isBaseJsonResult) {
            //模拟成功操作数据
            return new DataJsonResult(true);
        }
        //数据类型返回值
        if (StringUtils.isBlank(requestTime)) {
            //不要求请求时间属性
            TrialDocument trialDocument = trialDao.queryTrialData(module);
            if (trialDocument != null) {
                return new DataJsonResult(true, trialDocument.getData());
            }
        } else {
            TrialDocument trialDocument = trialDao.queryTrialData(module, requestTimeValue);
            if (trialDocument != null) {
                return new DataJsonResult(true, trialDocument.getData());
            }
        }
        return new DataJsonResult(false);
    }

    // TrialDataService 仅负责加载原始样板间数据，不再承担各业务模块的后置加工逻辑。
}
