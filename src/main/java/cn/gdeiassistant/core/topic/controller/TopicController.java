package cn.gdeiassistant.core.topic.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.common.tools.Utils.PageUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
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

    private JsonResult failure(HttpServletRequest request, String message) {
        return new JsonResult(false, BackendTextLocalizer.localizeMessage(message, request != null ? request.getHeader("Accept-Language") : null));
    }

    private int requirePositiveId(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        return id;
    }

    private int requireImageIndex(int index) {
        if (index < 1 || index > 9) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        return index;
    }

    @RequestMapping(value = "/api/topic/profile/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<TopicVO>> getMyTopicList(HttpServletRequest request
            , @PathVariable("start") int start, @PathVariable("size") int size) {
        size = PageUtils.normalizePageSize(start, size);
        String sessionId = (String) request.getAttribute("sessionId");
        List<TopicVO> list = topicService.queryMyTopicList(sessionId, start, size);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/topic/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<TopicVO> getTopicDetail(HttpServletRequest request, @PathVariable("id") int id) throws DataNotExistException {
        id = requirePositiveId(id);
        String sessionId = (String) request.getAttribute("sessionId");
        TopicVO vo = topicService.queryTopicById(id, sessionId);
        return new DataJsonResult<>(true, vo);
    }

    @RequestMapping(value = "/api/topic/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<TopicVO>> queryTopic(HttpServletRequest request, @PathVariable("start") int start
            , @PathVariable("size") int size) {
        size = PageUtils.normalizePageSize(start, size);
        String sessionId = (String) request.getAttribute("sessionId");
        List<TopicVO> list = topicService.queryTopic(sessionId, start, size);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/topic/keyword/{keyword}/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<TopicVO>> queryTopicByKeyword(HttpServletRequest request, @PathVariable("start") int start
            , @PathVariable("size") int size, @PathVariable("keyword") String keyword) {
        size = PageUtils.normalizePageSize(start, size);
        String sessionId = (String) request.getAttribute("sessionId");
        List<TopicVO> list = topicService.queryTopicByKeyword(sessionId, start, size, keyword);
        return new DataJsonResult<>(true, list);
    }

    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @RequestMapping(value = "/api/topic", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addTopic(HttpServletRequest request, @Validated TopicPublishDTO dto,
                               @RequestParam(value = "images", required = false) MultipartFile[] images,
                               @RequestParam(value = "imageKeys", required = false) String[] imageKeys) throws IOException {
        int actualImageCount = imageKeys != null && imageKeys.length > 0 ? imageKeys.length : (images == null ? 0 : images.length);
        if (actualImageCount > 9) {
            return failure(request, "不合法的图片文件");
        }
        if (images != null && images.length > 0) {
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty() || file.getSize() >= ValueConstantUtils.MAX_IMAGE_SIZE) {
                    return failure(request, "不合法的图片文件");
                }
            }
        }
        if (imageKeys != null && imageKeys.length > 0) {
            for (String imageKey : imageKeys) {
                if (StringUtils.isBlank(imageKey)) {
                    return failure(request, "不合法的图片文件");
                }
            }
        }
        String sessionId = (String) request.getAttribute("sessionId");
        dto.setCount(actualImageCount);
        TopicVO vo = topicService.addTopic(dto, sessionId);
        try {
            if (images != null && images.length > 0) {
                for (int i = 1; i <= images.length; i++) {
                    topicService.uploadTopicItemPicture(vo.getId(), i, images[i - 1].getInputStream());
                }
            } else if (imageKeys != null && imageKeys.length > 0) {
                for (int i = 1; i <= imageKeys.length; i++) {
                    topicService.moveTopicItemPictureFromTempObject(vo.getId(), i, imageKeys[i - 1]);
                }
            }
        } catch (Exception e) {
            topicService.deleteTopicImages(vo.getId(), dto.getCount());
            topicService.deleteTopic(vo.getId());
            return failure(request, "话题图片上传失败");
        }
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/topic/id/{id}/like", method = RequestMethod.POST)
    public JsonResult likeTopic(HttpServletRequest request, @PathVariable("id") int id) throws DataNotExistException {
        id = requirePositiveId(id);
        String sessionId = (String) request.getAttribute("sessionId");
        topicService.likeTopic(id, sessionId);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/topic/id/{id}/index/{index}/image", method = RequestMethod.GET)
    public DataJsonResult<String> getTopicImage(HttpServletRequest request, @PathVariable("id") int id, @PathVariable("index") int index) {
        id = requirePositiveId(id);
        index = requireImageIndex(index);
        String url = topicService.downloadTopicItemPicture(id, index);
        return new DataJsonResult<>(true, url);
    }
}
