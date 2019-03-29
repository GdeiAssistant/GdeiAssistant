package edu.gdei.gdeiassistant.Controller.KaoYan;

import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import edu.gdei.gdeiassistant.Pojo.Entity.KaoYan;
import edu.gdei.gdeiassistant.Pojo.KaoYanQuery.KaoYanQuery;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Service.KaoYan.KaoYanService;
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
