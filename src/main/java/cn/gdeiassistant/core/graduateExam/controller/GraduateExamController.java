package cn.gdeiassistant.core.graduateExam.controller;

import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.common.pojo.Entity.Postgraduate;
import cn.gdeiassistant.core.graduateExam.pojo.GraduateExamQuery;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.graduateExam.service.GraduateExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraduateExamController {

    @Autowired
    private GraduateExamService graduateExamService;

    @PostMapping("/api/kaoyan/query")
    public DataJsonResult<Postgraduate> queryPostgraduateScore(@Validated @RequestBody GraduateExamQuery graduateExamQuery)
            throws NetWorkTimeoutException, ServerErrorException, ErrorQueryConditionException {
        Postgraduate postgraduate = graduateExamService.queryPostgraduateScore(
                graduateExamQuery.getName(),
                graduateExamQuery.getExamNumber(),
                graduateExamQuery.getIdNumber()
        );
        return new DataJsonResult<>(true, postgraduate);
    }
}
