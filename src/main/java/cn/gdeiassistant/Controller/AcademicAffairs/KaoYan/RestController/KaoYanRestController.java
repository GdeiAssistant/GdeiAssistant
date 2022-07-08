package cn.gdeiassistant.Controller.AcademicAffairs.KaoYan.RestController;

import cn.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.Exception.CommonException.ServerErrorException;
import cn.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import cn.gdeiassistant.Pojo.Entity.KaoYan;
import cn.gdeiassistant.Pojo.KaoYanQuery.KaoYanQuery;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Service.AcademicAffairs.KaoYan.KaoYanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KaoYanRestController {

    @Autowired
    private KaoYanService kaoYanService;

    @RequestMapping(value = "/rest/kaoyanquery", method = RequestMethod.POST)
    public DataJsonResult<KaoYan> KaoYanScoreQuery(@Validated KaoYanQuery kaoYanQuery) throws NetWorkTimeoutException, ServerErrorException, ErrorQueryConditionException {
        KaoYan kaoYan = kaoYanService.KaoYanScoreQuery(kaoYanQuery.getName(), kaoYanQuery.getExamNumber(), kaoYanQuery.getIdNumber());
        return new DataJsonResult<>(true, kaoYan);
    }
}
