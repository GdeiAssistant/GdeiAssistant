package cn.gdeiassistant.core.dataquery.service;

import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.core.dataquery.pojo.YellowPageType;
import cn.gdeiassistant.common.pojo.entity.ElectricFees;
import cn.gdeiassistant.common.pojo.entity.YellowPage;
import cn.gdeiassistant.core.electricfees.mapper.ElectricFeesMapper;
import cn.gdeiassistant.core.yellowpage.pojo.entity.YellowPageCategory;
import cn.gdeiassistant.core.yellowpage.service.YellowPageService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import cn.gdeiassistant.common.i18n.Messages;

@Service
public class DataQueryService {

    private final ElectricFeesMapper electricFeesMapper;

    private final YellowPageService yellowPageService;

    public DataQueryService(ElectricFeesMapper electricFeesMapper, YellowPageService yellowPageService) {
        this.electricFeesMapper = electricFeesMapper;
        this.yellowPageService = yellowPageService;
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
        List<YellowPage> yellowPageList = yellowPageService.selectAllYellowPageList();
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
        List<YellowPageCategory> categories = yellowPageService.selectYellowPageCategories();
        if (categories != null && !categories.isEmpty()) {
            List<YellowPageType> yellowPageTypeList = new ArrayList<>(categories.size());
            for (YellowPageCategory category : categories) {
                YellowPageType type = new YellowPageType();
                type.setTypeCode(category.getTypeCode());
                type.setTypeName(category.getTypeName());
                yellowPageTypeList.add(type);
            }
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
        yellowPageService.insertYellowPageBatch(yellowPageList);
    }
}
