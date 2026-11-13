package cn.gdeiassistant.core.dataquery.service;

import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.core.dataquery.pojo.YellowPageType;
import cn.gdeiassistant.common.pojo.entity.ElectricFees;
import cn.gdeiassistant.common.pojo.entity.YellowPage;
import cn.gdeiassistant.core.electricfees.mapper.ElectricFeesMapper;
import cn.gdeiassistant.core.yellowpage.mapper.YellowPageMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import cn.gdeiassistant.common.i18n.Messages;

@Service
public class DataQueryService {

    private final ElectricFeesMapper electricFeesMapper;

    private final YellowPageMapper yellowPageMapper;

    public DataQueryService(ElectricFeesMapper electricFeesMapper, YellowPageMapper yellowPageMapper) {
        this.electricFeesMapper = electricFeesMapper;
        this.yellowPageMapper = yellowPageMapper;
    }

    /**
     * 查询电费信息
     *
     * @param name
     * @param number
     * @param year
     * @return
     * @throws DataNotExistException
     */
    public ElectricFees queryElectricFees(String name, Long number, Integer year) throws DataNotExistException {
        ElectricFees electricFees = electricFeesMapper.selectElectricFees(name, number, year);
        if (electricFees != null) {
            return electricFees;
        }
        throw new DataNotExistException(Messages.get("dataquery.electricityDataMissing"));
    }

    /**
     * 查询黄页列表信息
     *
     * @return
     */
    public List<YellowPage> queryYellowPageList() throws DataNotExistException {
        List<YellowPage> yellowPageList = yellowPageMapper.selectAllYellowPageList();
        if (yellowPageList != null && !yellowPageList.isEmpty()) {
            return yellowPageList;
        }
        throw new DataNotExistException(Messages.get("dataquery.yellowPagesListMissing"));
    }

    /**
     * 查询黄页分类信息
     *
     * @return
     * @throws DataNotExistException
     */
    public List<YellowPageType> queryYellowPageType() throws DataNotExistException {
        List<YellowPageType> yellowPageTypeList = yellowPageMapper.selectYellowPageType();
        if (yellowPageTypeList != null && !yellowPageTypeList.isEmpty()) {
            return yellowPageTypeList;
        }
        throw new DataNotExistException(Messages.get("dataquery.yellowPagesCategoriesMissing"));
    }

    /**
     * 批量插入电费数据
     *
     * @param electricFeesList
     */
    public void insertElectricFeesBatch(List<ElectricFees> electricFeesList) {
        electricFeesMapper.insertElectricFeesBatch(electricFeesList);
    }

    /**
     * 批量插入黄页数据
     *
     * @param yellowPageList
     */
    public void insertYellowPageBatch(List<YellowPage> yellowPageList) {
        yellowPageMapper.insertYellowPageBatch(yellowPageList);
    }
}
