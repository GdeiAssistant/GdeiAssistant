package edu.gdei.gdeiassistant.Service.Reading;

import edu.gdei.gdeiassistant.Pojo.Entity.Reading;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistantData.Reading.ReadingMapper;
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

    public List<Reading> LoadingAllReadingList(){
        return readingMapper.selectReadingList();
    }

    /**
     * 保存专题阅读信息
     * @param reading
     */
    public void SaveReadingInfo(Reading reading) {
        readingMapper.insertReading(reading);
    }
}
