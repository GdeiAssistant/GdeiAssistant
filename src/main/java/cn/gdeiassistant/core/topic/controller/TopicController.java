package cn.gdeiassistant.core.topic.controller;

import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.topic.pojo.dto.TopicPublishDTO;
import cn.gdeiassistant.core.topic.pojo.vo.TopicVO;
import cn.gdeiassistant.core.topic.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
public class TopicController {

    @Autowired
    private TopicService topicService;

    @RequestMapping(value = "/api/topic/profile/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<TopicVO>> getMyTopicList(HttpServletRequest request
            , @PathVariable("start") int start, @PathVariable("size") int size) {
        String sessionId = (String) request.getAttribute("sessionId");
        List<TopicVO> list = topicService.queryMyTopicList(sessionId, start, size);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/topic/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<TopicVO> getTopicDetail(HttpServletRequest request, @PathVariable("id") int id) throws DataNotExistException {
        String sessionId = (String) request.getAttribute("sessionId");
        TopicVO vo = topicService.queryTopicById(id, sessionId);
        return new DataJsonResult<>(true, vo);
    }

    @RequestMapping(value = "/api/topic/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<TopicVO>> queryTopic(HttpServletRequest request, @PathVariable("start") int start
            , @PathVariable("size") int size) {
        String sessionId = (String) request.getAttribute("sessionId");
        List<TopicVO> list = topicService.queryTopic(sessionId, start, size);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/topic/keyword/{keyword}/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<TopicVO>> queryTopicByKeyword(HttpServletRequest request, @PathVariable("start") int start
            , @PathVariable("size") int size, @PathVariable("keyword") String keyword) {
        String sessionId = (String) request.getAttribute("sessionId");
        List<TopicVO> list = topicService.queryTopicByKeyword(sessionId, start, size, keyword);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/topic", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addTopic(HttpServletRequest request, @Validated TopicPublishDTO dto, MultipartFile[] images) throws IOException {
        if (images != null && images.length > 0) {
            if (images.length > 9) {
                return new JsonResult(false, "不合法的图片文件");
            }
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty() || file.getSize() >= ValueConstantUtils.MAX_IMAGE_SIZE) {
                    return new JsonResult(false, "不合法的图片文件");
                }
            }
        }
        String sessionId = (String) request.getAttribute("sessionId");
        TopicVO vo = topicService.addTopic(dto, sessionId);
        if (images != null && images.length > 0) {
            for (int i = 1; i <= images.length; i++) {
                topicService.uploadTopicItemPicture(vo.getId(), i, images[i - 1].getInputStream());
            }
        }
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/topic/id/{id}/like", method = RequestMethod.POST)
    public JsonResult likeTopic(HttpServletRequest request, @PathVariable("id") int id) throws DataNotExistException {
        String sessionId = (String) request.getAttribute("sessionId");
        topicService.likeTopic(id, sessionId);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/topic/id/{id}/index/{index}/image", method = RequestMethod.GET)
    public DataJsonResult<String> getTopicImage(HttpServletRequest request, @PathVariable("id") int id, @PathVariable("index") int index) {
        String url = topicService.downloadTopicItemPicture(id, index);
        return new DataJsonResult<>(true, url);
    }
}
