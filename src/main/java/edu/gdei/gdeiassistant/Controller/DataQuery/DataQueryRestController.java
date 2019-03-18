package edu.gdei.gdeiassistant.Controller.DataQuery;

import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.DataQuery.YellowPageResult;
import edu.gdei.gdeiassistant.Pojo.DataQuery.YellowPageType;
import edu.gdei.gdeiassistant.Pojo.Entity.ElectricFees;
import edu.gdei.gdeiassistant.Pojo.Entity.YellowPage;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Service.DataQuery.DataQueryService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
public class DataQueryRestController {

    @Autowired
    private DataQueryService dataQueryService;

    /**
     * 查询电费数据
     *
     * @param name
     * @param number
     * @param year
     * @return
     * @throws DataNotExistException
     */
    @RequestMapping(value = "/api/data/electricfees", method = RequestMethod.POST)
    public DataJsonResult<ElectricFees> QueryElectricFeesData(@Validated @NotBlank @Length(max = 5) String name
            , @Validated @NotBlank Long number, @Validated @NotNull @Range(min = 2016, max = 2050) Integer year) throws DataNotExistException {
        ElectricFees electricFees = dataQueryService.QueryElectricFees(name, number, year);
        return new DataJsonResult<>(true, electricFees);
    }

    /**
     * 查询黄页信息
     *
     * @return
     */
    @RequestMapping(value = "/api/data/yellowpage", method = RequestMethod.GET)
    public DataJsonResult<YellowPageResult> QueryYellowPageListData() throws DataNotExistException {
        YellowPageResult yellowPageResult = new YellowPageResult();
        List<YellowPage> yellowPageList = dataQueryService.QueryYellowPageList();
        List<YellowPageType> yellowPageTypeList = dataQueryService.QueryYellowPageType();
        yellowPageResult.setData(yellowPageList);
        yellowPageResult.setType(yellowPageTypeList);
        return new DataJsonResult<>(true, yellowPageResult);
    }

}
