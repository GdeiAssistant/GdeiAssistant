package cn.gdeiassistant.core.dataquery.controller;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.core.dataquery.pojo.YellowPageResult;
import cn.gdeiassistant.core.dataquery.pojo.YellowPageType;
import cn.gdeiassistant.common.pojo.Entity.ElectricFees;
import cn.gdeiassistant.common.pojo.Entity.YellowPage;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.dataquery.service.DataQueryService;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@RestController
public class DataQueryController {

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
    public DataJsonResult<ElectricFees> queryElectricFeesData(@Validated @NotBlank @Length(max = 5) String name
            , @Validated @NotNull Long number, @Validated @NotNull @Range(min = 2016, max = 2050) Integer year) throws DataNotExistException {
        ElectricFees electricFees = dataQueryService.queryElectricFees(name, number, year);
        return new DataJsonResult<>(true, electricFees);
    }

    /**
     * 查询黄页信息
     *
     * @return
     */
    @RequestMapping(value = "/api/data/yellowpage", method = RequestMethod.GET)
    public DataJsonResult<YellowPageResult> queryYellowPageListData() throws DataNotExistException {
        YellowPageResult yellowPageResult = new YellowPageResult();
        List<YellowPage> yellowPageList = dataQueryService.queryYellowPageList();
        List<YellowPageType> yellowPageTypeList = dataQueryService.queryYellowPageType();
        yellowPageResult.setData(yellowPageList);
        yellowPageResult.setType(yellowPageTypeList);
        return new DataJsonResult<>(true, yellowPageResult);
    }

}
