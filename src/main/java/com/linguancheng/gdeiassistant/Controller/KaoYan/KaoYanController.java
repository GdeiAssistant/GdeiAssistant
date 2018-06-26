package com.linguancheng.gdeiassistant.Controller.KaoYan;

import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.KaoYan;
import com.linguancheng.gdeiassistant.Pojo.KaoYanQuery.KaoYanQuery;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Service.KaoYan.KaoYanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class KaoYanController {

    @Autowired
    private KaoYanService kaoYanService;

    @RequestMapping(value = "/kaoyan", method = RequestMethod.GET)
    public ModelAndView ResolveKaoYanPage() {
        return new ModelAndView("KaoYan/kaoyan");
    }

    @RequestMapping(value = "/rest/kaoyanquery", method = RequestMethod.POST)
    @ResponseBody
    public DataJsonResult<KaoYan> KaoYanScoreQuery(@Validated KaoYanQuery kaoYanQuery, BindingResult bindingResult) {
        DataJsonResult<KaoYan> jsonResult = new DataJsonResult<>();
        if (bindingResult.hasErrors()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("请求参数不合法");
        } else {
            BaseResult<KaoYan, ServiceResultEnum> result = kaoYanService.KaoYanScoreQuery(kaoYanQuery.getName(), kaoYanQuery.getExamNumber(), kaoYanQuery.getIdNumber());
            switch (result.getResultType()) {
                case SUCCESS:
                    jsonResult.setSuccess(true);
                    jsonResult.setData(result.getResultData());
                    break;

                case SERVER_ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("研招网查询系统维护中，请稍后再试");
                    break;

                case TIME_OUT:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("网络连接超时，请重试");
                    break;

                case ERROR_CONDITION:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("提交的考生信息有误，请检查");
                    break;

                case EMPTY_RESULT:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("无查询结果，请确认招生单位已开通初试成绩查询功能");
                    break;
            }
        }
        return jsonResult;
    }
}
