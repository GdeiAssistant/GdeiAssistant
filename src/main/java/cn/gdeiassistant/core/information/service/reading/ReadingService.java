package cn.gdeiassistant.core.information.service.Reading;

import cn.gdeiassistant.core.information.converter.ReadingConverter;
import cn.gdeiassistant.core.information.pojo.entity.ReadingEntity;
import cn.gdeiassistant.core.information.pojo.vo.ReadingVO;
import cn.gdeiassistant.core.reading.mapper.ReadingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
public class ReadingService {

    @Resource(name = "readingMapper")
    private ReadingMapper readingMapper;

    @Autowired
    private ReadingConverter readingConverter;

    /**
     * 获取最新的专题阅读列表
     */
    public List<ReadingVO> loadLatestReadingList() {
        List<ReadingEntity> list = readingMapper.selectLatestReadingList();
        return list == null ? Collections.emptyList() : readingConverter.toVOList(list);
    }

    public List<ReadingVO> loadAllReadingList() {
        List<ReadingEntity> list = readingMapper.selectReadingList();
        return list == null ? Collections.emptyList() : readingConverter.toVOList(list);
    }
}
