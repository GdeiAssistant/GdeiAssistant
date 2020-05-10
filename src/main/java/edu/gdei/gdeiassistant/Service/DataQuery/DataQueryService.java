package edu.gdei.gdeiassistant.Service.DataQuery;

import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.DataQuery.YellowPageType;
import edu.gdei.gdeiassistant.Pojo.Entity.ElectricFees;
import edu.gdei.gdeiassistant.Pojo.Entity.YellowPage;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantData.ElectricFees.ElectricFeesMapper;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantData.YellowPage.YellowPageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataQueryService {

    @Autowired
    private ElectricFeesMapper electricFeesMapper;

    @Autowired
    private YellowPageMapper yellowPageMapper;

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
     * 查询黄页列表信息
     *
     * @return
     */
    public List<YellowPage> QueryYellowPageList() throws DataNotExistException {
        List<YellowPage> yellowPageList = yellowPageMapper.selectAllYellowPageList();
        if (yellowPageList != null && !yellowPageList.isEmpty()) {
            return yellowPageList;
        }
        throw new DataNotExistException("没有黄页列表信息");
    }

    /**
     * 查询黄页分类信息
     *
     * @return
     * @throws DataNotExistException
     */
    public List<YellowPageType> QueryYellowPageType() throws DataNotExistException {
        List<YellowPageType> yellowPageTypeList = yellowPageMapper.selectYellowPageType();
        if (yellowPageTypeList != null && !yellowPageTypeList.isEmpty()) {
            return yellowPageTypeList;
        }
        throw new DataNotExistException("没有黄页分类信息");
    }

    /**
     * 批量插入电费数据
     *
     * @param electricFeesList
     */
    public void InsertElectricFeesBatch(List<ElectricFees> electricFeesList) {
        electricFeesMapper.insertElectricFeesBatch(electricFeesList);
    }

    /**
     * 批量插入黄页数据
     *
     * @param yellowPageList
     */
    public void InsertYellowPageBatch(List<YellowPage> yellowPageList) {
        yellowPageMapper.insertYellowPageBatch(yellowPageList);
    }
}
