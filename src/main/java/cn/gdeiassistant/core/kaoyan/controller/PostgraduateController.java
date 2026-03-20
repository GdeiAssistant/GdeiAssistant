package cn.gdeiassistant.core.kaoyan.controller;

import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.common.pojo.Entity.Postgraduate;
import cn.gdeiassistant.core.kaoyanquery.pojo.PostgraduateQuery;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.kaoyan.service.PostgraduateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostgraduateController {

    @Autowired
    private PostgraduateService postgraduateService;

    @PostMapping("/api/kaoyan/query")
    public DataJsonResult<Postgraduate> queryPostgraduateScore(@Validated @RequestBody PostgraduateQuery postgraduateQuery)
            throws NetWorkTimeoutException, ServerErrorException, ErrorQueryConditionException {
        Postgraduate postgraduate = postgraduateService.queryPostgraduateScore(
                postgraduateQuery.getName(),
                postgraduateQuery.getExamNumber(),
                postgraduateQuery.getIdNumber()
        );
        return new DataJsonResult<>(true, postgraduate);
    }
}
