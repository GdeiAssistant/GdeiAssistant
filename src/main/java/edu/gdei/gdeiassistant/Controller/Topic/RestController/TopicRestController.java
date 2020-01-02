package edu.gdei.gdeiassistant.Controller.Topic.RestController;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Annotation.RestCheckAuthentication;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.Topic;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Topic.TopicService;
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

    private final int MAX_PICTURE_SIZE = 1024 * 1024 * 5;

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
    @RestCheckAuthentication(name = "topic")
    public DataJsonResult<List<Topic>> QueryTopic(HttpServletRequest request, @PathVariable("start") int start
            , @PathVariable("size") int size) throws WsgException {
        String username = (String) request.getSession().getAttribute("username");
        List<Topic> topicList = topicService.QueryTopic(start, size, username);
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
    @RestCheckAuthentication(name = "topic")
    public DataJsonResult<List<Topic>> QueryTopicByKeyword(HttpServletRequest request, @PathVariable("start") int start
            , @PathVariable("size") int size, @PathVariable("keyword") String keyword) throws WsgException {
        String username = (String) request.getSession().getAttribute("username");
        List<Topic> topicList = topicService.QueryTopicByKeyword(start, size, username, keyword);
        return new DataJsonResult<>(true, topicList);
    }

    /**
     * 添加话题信息
     *
     * @param request
     * @param topic
     * @param images
     * @return
     * @throws WsgException
     * @throws IOException
     */
    @RequestMapping(value = "/api/topic", method = RequestMethod.POST)
    @RestCheckAuthentication(name = "topic")
    public JsonResult AddTopic(HttpServletRequest request, @Validated Topic topic, MultipartFile[] images) throws WsgException, IOException {
        if (images != null) {
            if (images.length > 9) {
                return new JsonResult(false, "不合法的图片文件");
            }
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty() || file.getSize() >= MAX_PICTURE_SIZE) {
                    return new JsonResult(false, "不合法的图片文件");
                }
            }
        }
        String username = (String) request.getSession().getAttribute("username");
        //插入话题信息
        Topic data = topicService.AddTopic(topic, username);
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
     */
    @RequestMapping(value = "/api/topic/id/{id}/like", method = RequestMethod.POST)
    @RestCheckAuthentication(name = "topic")
    public JsonResult LikeTopic(HttpServletRequest request, @PathVariable("id") int id) throws DataNotExistException, WsgException {
        String username = (String) request.getSession().getAttribute("username");
        topicService.LikeTopic(id, username);
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
    @RestCheckAuthentication(name = "topic")
    public DataJsonResult<String> GetTopicImage(HttpServletRequest request, @PathVariable("id") int id, @PathVariable("index") int index) {
        String url = topicService.DownloadTopicItemPicture(id, index);
        return new DataJsonResult<>(true, url);
    }

}
