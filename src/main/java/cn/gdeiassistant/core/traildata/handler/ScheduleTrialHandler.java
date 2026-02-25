package cn.gdeiassistant.core.trailData.handler;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Document.CustomScheduleDocument;
import cn.gdeiassistant.common.pojo.Entity.Schedule;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.schedulequery.pojo.ScheduleQueryResult;
import cn.gdeiassistant.core.schedulequery.service.ScheduleService;
import cn.gdeiassistant.common.tools.Utils.ScheduleUtils;
import cn.gdeiassistant.common.tools.Utils.WeekUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 课表模块样板间处理器：从 trial 集合加载原始课表，并在此处合并自定义课表与周次过滤。
 */
@Component
public class ScheduleTrialHandler extends AbstractTrialModuleHandler {

    @Autowired
    protected ScheduleService scheduleService;

    @Override
    public String getModuleKey() {
        return "schedule";
    }

    @Override
    public DataJsonResult<?> handle(String sessionId,
                                    HttpServletRequest request,
                                    TrialData trialData,
                                    ProceedingJoinPoint joinPoint) {
        DataJsonResult raw = trialDataService.loadTrialData("schedule", false, null, 0);
        if (raw == null || raw.getData() == null) {
            return buildError(TrialErrorCode.DATA_NOT_FOUND);
        }
        String json = new Gson().toJson(raw);
        DataJsonResult<ScheduleQueryResult> scheduleJsonData = new Gson().fromJson(json,
                new TypeToken<DataJsonResult<ScheduleQueryResult>>() {
                }.getType());

        if (scheduleJsonData.getData() == null) {
            return buildError(TrialErrorCode.DATA_NOT_FOUND);
        }

        // 合并自定义课表
        CustomScheduleDocument customScheduleDocument = scheduleService.getCustomSchedule(sessionId);
        if (customScheduleDocument != null && customScheduleDocument.getScheduleMap() != null) {
            List<Schedule> scheduleList = new ArrayList<>(customScheduleDocument.getScheduleMap().values());
            scheduleJsonData.getData().getScheduleList().addAll(scheduleList);
        }

        // 根据当前周或请求参数 week 过滤课表
        Integer week = null;
        try {
            String weekParam = request.getParameter("week");
            if (weekParam != null && !weekParam.trim().isEmpty()) {
                week = Integer.parseInt(weekParam.trim());
            }
        } catch (NumberFormatException ignore) {
        }
        if (week == null) {
            week = WeekUtils.getCurrentWeek();
        }
        scheduleJsonData.getData().setScheduleList(
                ScheduleUtils.getSpecifiedWeekSchedule(scheduleJsonData.getData().getScheduleList(), week)
        );
        return scheduleJsonData;
    }
}

