package cn.gdeiassistant.Controller.Topic.RestController;

import cn.gdeiassistant.Annotation.RecordIPAddress;
import cn.gdeiassistant.Constant.ValueConstantUtils;
import cn.gdeiassistant.Enum.IPAddress.IPAddressEnum;
import cn.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.Pojo.Entity.Topic;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.Socialising.Topic.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
public class TopicRestController {

    @Autowired
    private TopicService topicService;

    /**
     * 加载话题信息
     *
     * @param request
     * @param start
     * @param size
     * @return
     */
    @RequestMapping(value = "/api/topic/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<Topic>> QueryTopic(HttpServletRequest request, @PathVariable("start") int start
            , @PathVariable("size") int size) {
        List<Topic> topicList = topicService.QueryTopic(request.getSession().getId(), start, size);
        return new DataJsonResult<>(true, topicList);
    }

    /**
     * 根据关键词加载话题信息
     *
     * @param request
     * @param start
     * @param size
     * @param keyword
     * @return
     */
    @RequestMapping(value = "/api/topic/keyword/{keyword}/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<Topic>> QueryTopicByKeyword(HttpServletRequest request, @PathVariable("start") int start
            , @PathVariable("size") int size, @PathVariable("keyword") String keyword) {
        List<Topic> topicList = topicService.QueryTopicByKeyword(request.getSession().getId(), start, size, keyword);
        return new DataJsonResult<>(true, topicList);
    }

    /**
     * 添加话题信息
     *
     * @param request
     * @param topic
     * @param images
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/api/topic", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult AddTopic(HttpServletRequest request, @Validated Topic topic, MultipartFile[] images) throws IOException {
        if (images != null) {
            if (images.length > 9) {
                return new JsonResult(false, "不合法的图片文件");
            }
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty() || file.getSize() >= ValueConstantUtils.MAX_IMAGE_SIZE) {
                    return new JsonResult(false, "不合法的图片文件");
                }
            }
        }
        //插入话题信息
        Topic data = topicService.AddTopic(topic, request.getSession().getId());
        //上传图片信息
        for (int i = 1; i <= images.length; i++) {
            topicService.UploadTopicItemPicture(data.getId(), i, images[i - 1].getInputStream());
        }
        return new JsonResult(true);
    }

    /**
     * 点赞话题信息
     *
     * @param request
     * @param id
     * @return
     * @throws DataNotExistException
     */
    @RequestMapping(value = "/api/topic/id/{id}/like", method = RequestMethod.POST)
    public JsonResult LikeTopic(HttpServletRequest request, @PathVariable("id") int id) throws DataNotExistException {
        topicService.LikeTopic(id, request.getSession().getId());
        return new JsonResult(true);
    }

    /**
     * 获取话题信息图片地址
     *
     * @param request
     * @param id
     * @param index
     * @return
     */
    @RequestMapping(value = "/api/topic/id/{id}/index/{index}/image", method = RequestMethod.GET)
    public DataJsonResult<String> GetTopicImage(HttpServletRequest request, @PathVariable("id") int id, @PathVariable("index") int index) {
        String url = topicService.DownloadTopicItemPicture(id, index);
        return new DataJsonResult<>(true, url);
    }

}
