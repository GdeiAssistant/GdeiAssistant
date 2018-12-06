package com.gdeiassistant.gdeiassistant.Controller.DataQuery;

import com.gdeiassistant.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.ElectricFees;
import com.gdeiassistant.gdeiassistant.Pojo.Result.DataJsonResult;
import com.gdeiassistant.gdeiassistant.Service.DataQuery.DataQueryService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

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
    @RequestMapping(value = "/api/data/electricfees", method = RequestMethod.GET)
    public DataJsonResult<ElectricFees> QueryElectricFeesData(@Validated @NotBlank @Length(max = 5) String name
            , @Validated @NotBlank Long number, @Validated @NotNull @Range(min = 2016, max = 2050) Integer year) throws DataNotExistException {
        ElectricFees electricFees = dataQueryService.QueryElectricFees(name, Long.valueOf(number), year);
        return new DataJsonResult<>(true, electricFees);
    }
}
