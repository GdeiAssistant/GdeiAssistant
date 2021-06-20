package cn.gdeiassistant.Service.TrailData;

import cn.gdeiassistant.Pojo.CardQuery.CardQuery;
import cn.gdeiassistant.Pojo.Entity.Grade;
import cn.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import cn.gdeiassistant.Service.ScheduleQuery.ScheduleService;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import cn.gdeiassistant.Pojo.Entity.Schedule;
import cn.gdeiassistant.Tools.StringUtils;
import cn.gdeiassistant.Tools.WeekUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

@Service
public class TrialDataService {

    @Autowired
    private Environment environment;

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 解析并加载模拟结果数据
     *
     * @param module
     * @param isBaseJsonResult
     * @param requestTime
     * @param requestTimeValue
     * @param responseTime
     * @param responseTimeValue
     * @param username
     * @return
     */
    public DataJsonResult ParseTrialData(String module, boolean isBaseJsonResult
            , String requestTime, Object requestTimeValue, String responseTime, Object responseTimeValue, String username) {
        //加载初始模拟数据
        DataJsonResult result = LoadInitialTrialData(module, isBaseJsonResult, requestTime, requestTimeValue);
        //进行模拟数据后加工处理
        result = ProcessTrialData(module, username, result, responseTime, responseTimeValue);
        return result;
    }

    /**
     * 加载初始模拟数据
     *
     * @param module
     * @param requestTime
     * @return
     */
    private DataJsonResult LoadInitialTrialData(String module, boolean isBaseJsonResult, String requestTime
            , Object requestTimeValue) {
        //基础类型返回值
        if (isBaseJsonResult) {
            //从配置文件加载对应的模拟数据值，并封装成基础类型返回值
            return new DataJsonResult(new Gson().fromJson(environment.getProperty("trial.data." + module)
                    , new TypeToken<JsonResult>() {
                    }.getType()));
        }
        //数据类型返回值
        if (StringUtils.isBlank(requestTime)) {
            //不要求请求时间属性
            return new Gson().fromJson(environment.getProperty("trial.data." + module)
                    , new TypeToken<DataJsonResult>() {
                    }.getType());
        }
        //要求时间属性，按照功能模块进行处理
        switch (module) {
            case "grade":
                //使用学年时间属性加载不同的初始模拟数据
                if (requestTimeValue == null) {
                    //使用默认的第一学年属性
                    requestTimeValue = 0;
                }
                return new Gson().fromJson(environment.getProperty("trial.data." + module + "." + requestTimeValue)
                        , new TypeToken<DataJsonResult>() {
                        }.getType());

            default:
                return new Gson().fromJson(environment.getProperty("trial.data." + module)
                        , new TypeToken<DataJsonResult>() {
                        }.getType());
        }
    }

    /**
     * 对初始模拟结果数据进行后加工处理
     *
     * @param module
     * @param username
     * @param result
     */
    private DataJsonResult ProcessTrialData(String module, String username, DataJsonResult result
            , String responseTime, Object responseTimeValue) {
        //填充返回值时间属性
        if (StringUtils.isNotBlank(responseTime)) {
            if (responseTimeValue == null) {
                //配置默认的响应时间属性
                switch (module) {
                    case "grade":
                        responseTimeValue = 0;
                        break;

                    case "schedule":
                        responseTimeValue = 1;
                        break;

                    case "cardQuery":
                        LocalDate localDate = LocalDate.now();
                        CardQuery cardQuery = new CardQuery();
                        cardQuery.setYear(localDate.getYear());
                        cardQuery.setMonth(localDate.getMonthValue());
                        cardQuery.setDate(localDate.getDayOfMonth());
                        responseTimeValue = cardQuery;
                        break;
                }
            }
            //将响应时间属性填充入模拟结果数据中与TrialData注解中responseTime参数的值相同的属性中
            ((LinkedTreeMap) result.getData()).put(responseTime, responseTimeValue);
        }
        String json = new Gson().toJson(result);
        //各模块的数据进一步加工逻辑
        switch (module) {
            case "grade":
                //成绩查询模块
                DataJsonResult<GradeQueryResult> gradeJsonData = new Gson().fromJson(json
                        , new TypeToken<DataJsonResult<GradeQueryResult>>() {
                        }.getType());
                //计算学期IGP和GPA
                double firstTermIGP = 0;
                double secondTermIGP = 0;
                double firstTermGPA = 0;
                double secondTermGPA = 0;
                double firstCreditSum = 0;
                double secondCreditSum = 0;
                for (int i = 0; i < gradeJsonData.getData().getFirstTermGradeList().size(); i++) {
                    //计算第一学期IGP
                    Grade grade = gradeJsonData.getData().getFirstTermGradeList().get(i);
                    firstTermIGP = firstTermIGP + (Double.parseDouble((grade.getGradeCredit()))
                            * Double.parseDouble(grade.getGradeGpa()));
                    firstCreditSum = firstCreditSum + Double.parseDouble(grade.getGradeCredit());
                    //计算第一学期GPA
                    if (firstCreditSum != 0) {
                        firstTermGPA = firstTermIGP / firstCreditSum;
                    }
                }
                for (int i = 0; i < gradeJsonData.getData().getSecondTermGradeList().size(); i++) {
                    //计算第二学期IGP
                    Grade grade = gradeJsonData.getData().getSecondTermGradeList().get(i);
                    secondTermIGP = secondTermIGP + (Double.parseDouble((grade.getGradeCredit()))
                            * Double.parseDouble(grade.getGradeGpa()));
                    secondCreditSum = secondCreditSum + Double.parseDouble(grade.getGradeCredit());
                    //计算第二学期GPA
                    if (secondCreditSum != 0) {
                        secondTermGPA = secondTermIGP / secondCreditSum;
                    }
                }
                //保留两位小数
                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                firstTermIGP = Double.parseDouble(decimalFormat.format(firstTermIGP));
                secondTermIGP = Double.parseDouble(decimalFormat.format(secondTermIGP));
                firstTermGPA = Double.parseDouble(decimalFormat.format(firstTermGPA));
                secondTermGPA = Double.parseDouble(decimalFormat.format(secondTermGPA));
                gradeJsonData.getData().setFirstTermIGP(firstTermIGP);
                gradeJsonData.getData().setSecondTermIGP(secondTermIGP);
                gradeJsonData.getData().setFirstTermGPA(firstTermGPA);
                gradeJsonData.getData().setSecondTermGPA(secondTermGPA);
                return gradeJsonData;

            case "schedule":
                //课表查询模块
                DataJsonResult<ScheduleQueryResult> scheduleJsonData = new Gson().fromJson(json
                        , new TypeToken<DataJsonResult<ScheduleQueryResult>>() {
                        }.getType());
                //添加自定义的课表信息
                List<Schedule> scheduleList = scheduleService.GetCustomScheduleList(username);
                scheduleJsonData.getData().getScheduleList().addAll(scheduleList);
                //筛选剔除非当前周数的课程
                if (responseTimeValue == null) {
                    scheduleJsonData.getData().setScheduleList(scheduleService.GetSpecifiedWeekSchedule
                            (scheduleJsonData.getData().getScheduleList(), WeekUtils.GetCurrentWeek()));
                } else {
                    scheduleJsonData.getData().setScheduleList(scheduleService.GetSpecifiedWeekSchedule
                            (scheduleJsonData.getData().getScheduleList(), (Integer) responseTimeValue));
                }
                return scheduleJsonData;

            default:
                return result;
        }
    }
}
