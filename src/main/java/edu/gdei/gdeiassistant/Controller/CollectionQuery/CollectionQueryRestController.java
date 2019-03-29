package edu.gdei.gdeiassistant.Controller.CollectionQuery;

import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import edu.gdei.gdeiassistant.Pojo.CollectionQuery.CollectionDetailQuery;
import edu.gdei.gdeiassistant.Pojo.CollectionQuery.CollectionQuery;
import edu.gdei.gdeiassistant.Pojo.CollectionQuery.CollectionQueryResult;
import edu.gdei.gdeiassistant.Pojo.Entity.CollectionDetail;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.CollectionQuery.CollectionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CollectionQueryRestController {

    @Autowired
    private CollectionQueryService collectionQueryService;

    /**
     * 馆藏图书详细信息查询Restful接口
     *
     * @param collectionDetailQuery
     * @return
     * @throws ServerErrorException
     * @throws NetWorkTimeoutException
     */
    @RequestMapping(value = "/rest/collectiondetail", method = RequestMethod.POST)
    public DataJsonResult<CollectionDetail> RestCollectionDetailQuery(@Validated CollectionDetailQuery collectionDetailQuery) throws ServerErrorException, NetWorkTimeoutException {
        CollectionDetail collectionDetail = collectionQueryService.CollectionDetailQuery(collectionDetailQuery);
        return new DataJsonResult<>(true, collectionDetail);
    }

    /**
     * 馆藏信息查询Restful接口
     *
     * @param collectionQuery
     * @return
     */
    @RequestMapping(value = "/rest/collectionquery", method = RequestMethod.POST)
    @ResponseBody
    public DataJsonResult<CollectionQueryResult> RestfulCollectionQuery(@Validated CollectionQuery collectionQuery) throws NetWorkTimeoutException, ServerErrorException, ErrorQueryConditionException {
        CollectionQueryResult collectionQueryResult = collectionQueryService.CollectionQuery(collectionQuery.getPage(), collectionQuery.getBookname());
        if (collectionQueryResult == null || collectionQueryResult.getCollectionList().isEmpty()) {
            return new DataJsonResult<>(new JsonResult(false, "查询结果为空"));
        }
        return new DataJsonResult<>(true, collectionQueryResult);
    }
}
