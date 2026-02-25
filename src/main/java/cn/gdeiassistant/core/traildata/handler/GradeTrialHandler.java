package cn.gdeiassistant.core.trailData.handler;

import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.constant.TrialErrorCode;
import cn.gdeiassistant.common.pojo.Entity.Grade;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.gradequery.pojo.GradeQueryResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;

/**
 * 成绩模块样板间处理器：测试账号统一从 trial 集合返回成绩数据，并在此处计算 GPA/IGP。
 */
@Component
public class GradeTrialHandler extends AbstractTrialModuleHandler {

    @Override
    public String getModuleKey() {
        return "grade";
    }

    @Override
    public DataJsonResult<?> handle(String sessionId,
                                    HttpServletRequest request,
                                    TrialData trialData,
                                    ProceedingJoinPoint joinPoint) {
        // 加载原始成绩样板数据
        DataJsonResult raw = trialDataService.loadTrialData("grade", false, null, 0);
        if (raw == null || raw.getData() == null) {
            // 样板间数据缺失时统一返回 DATA_NOT_FOUND，避免前端收到 null message
            return buildError(TrialErrorCode.DATA_NOT_FOUND);
        }
        String json = new Gson().toJson(raw);
        DataJsonResult<GradeQueryResult> gradeJsonData = new Gson().fromJson(json,
                new TypeToken<DataJsonResult<GradeQueryResult>>() {
                }.getType());

        if (gradeJsonData.getData() == null) {
            return gradeJsonData;
        }

        double firstTermIGP = 0;
        double secondTermIGP = 0;
        double firstTermGPA = 0;
        double secondTermGPA = 0;
        double firstCreditSum = 0;
        double secondCreditSum = 0;

        for (Grade grade : gradeJsonData.getData().getFirstTermGradeList()) {
            firstTermIGP += Double.parseDouble(grade.getGradeCredit()) * Double.parseDouble(grade.getGradeGpa());
            firstCreditSum += Double.parseDouble(grade.getGradeCredit());
            if (firstCreditSum != 0) {
                firstTermGPA = firstTermIGP / firstCreditSum;
            }
        }
        for (Grade grade : gradeJsonData.getData().getSecondTermGradeList()) {
            secondTermIGP += Double.parseDouble(grade.getGradeCredit()) * Double.parseDouble(grade.getGradeGpa());
            secondCreditSum += Double.parseDouble(grade.getGradeCredit());
            if (secondCreditSum != 0) {
                secondTermGPA = secondTermIGP / secondCreditSum;
            }
        }

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
    }
}

