package cn.gdeiassistant.core.graduateexam.controller;

import cn.gdeiassistant.common.exception.commonexception.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
import cn.gdeiassistant.common.exception.queryexception.ErrorQueryConditionException;
import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;
import cn.gdeiassistant.common.pojo.entity.Postgraduate;
import cn.gdeiassistant.core.graduateexam.pojo.GraduateExamQuery;
import cn.gdeiassistant.common.pojo.result.DataJsonResult;
import cn.gdeiassistant.core.graduateexam.service.GraduateExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraduateExamController {

    @Autowired
    private GraduateExamService graduateExamService;

    @PostMapping("/api/graduate-exam/query")
    public DataJsonResult<Postgraduate> queryPostgraduateScore(@Validated @RequestBody GraduateExamQuery graduateExamQuery)
            throws NetWorkTimeoutException, ServerErrorException, ErrorQueryConditionException, RecognitionException {
        Postgraduate postgraduate = graduateExamService.queryPostgraduateScore(
                graduateExamQuery.getName(),
                graduateExamQuery.getExamNumber(),
                graduateExamQuery.getIdNumber()
        );
        return new DataJsonResult<>(true, postgraduate);
    }
}
