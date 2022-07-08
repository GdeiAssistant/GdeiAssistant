package cn.gdeiassistant.Service.Information.Reading;

import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantData.Reading.ReadingMapper;
import cn.gdeiassistant.Pojo.Entity.Reading;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ReadingService {

    @Resource(name = "readingMapper")
    private ReadingMapper readingMapper;

    /**
     * 获取最新的专题阅读列表
     *
     * @return
     */
    public List<Reading> LoadingLatestReadingList() {
        return readingMapper.selectLatestReadingList();
    }

    public List<Reading> LoadingAllReadingList() {
        return readingMapper.selectReadingList();
    }
}
