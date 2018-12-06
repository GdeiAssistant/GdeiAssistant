package com.linguancheng.gdeiassistant.Service.DataQuery;

import com.linguancheng.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import com.linguancheng.gdeiassistant.Pojo.Entity.ElectricFees;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistantData.ElectricFees.ElectricFeesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataQueryService {

    @Autowired
    private ElectricFeesMapper electricFeesMapper;

    /**
     * 查询电费信息
     *
     * @param name
     * @param number
     * @param year
     * @return
     * @throws DataNotExistException
     */
    public ElectricFees QueryElectricFees(String name, Long number, Integer year) throws DataNotExistException {
        ElectricFees electricFees = electricFeesMapper.selectElectricFees(name, number, year);
        if (electricFees != null) {
            return electricFees;
        }
        throw new DataNotExistException("查询的电费数据不存在");
    }

    /**
     * 批量插入电费数据
     *
     * @param electricFeesList
     */
    public void InsertElectricFeesBatch(List<ElectricFees> electricFeesList) {
        electricFeesMapper.insertElectricFeesBatch(electricFeesList);
    }
}
