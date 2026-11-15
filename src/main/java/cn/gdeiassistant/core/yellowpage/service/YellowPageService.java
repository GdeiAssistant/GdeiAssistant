package cn.gdeiassistant.core.yellowpage.service;

import cn.gdeiassistant.common.pojo.entity.YellowPage;
import cn.gdeiassistant.core.yellowpage.mapper.YellowPageMapper;
import cn.gdeiassistant.core.yellowpage.pojo.entity.YellowPageCategory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class YellowPageService {

    private final YellowPageMapper yellowPageMapper;

    public YellowPageService(YellowPageMapper yellowPageMapper) {
        this.yellowPageMapper = yellowPageMapper;
    }

    public List<YellowPage> selectAllYellowPageList() {
        return yellowPageMapper.selectAllYellowPageList();
    }

    public List<YellowPageCategory> selectYellowPageCategories() {
        return yellowPageMapper.selectYellowPageType();
    }

    public void insertYellowPageBatch(List<YellowPage> yellowPageList) {
        yellowPageMapper.insertYellowPageBatch(yellowPageList);
    }
}
