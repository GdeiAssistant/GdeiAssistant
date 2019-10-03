package edu.gdei.gdeiassistant.Controller.Photograph.RestController;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Annotation.RestCheckAuthentication;
import edu.gdei.gdeiassistant.Pojo.Entity.Photograph;
import edu.gdei.gdeiassistant.Pojo.Entity.PhotographComment;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Photograph.PhotographService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@RestController
public class PhotographRestController {

    private final int MAX_PICTURE_SIZE = 1024 * 1024 * 5;
    @Autowired
    private PhotographService photographService;

    /**
     * 查询照片总数统计数据
     */
    @RequestMapping(value = "/api/photograph/statistics/photos", method = RequestMethod.GET)
    @RestCheckAuthentication(name = "photograph")
    public DataJsonResult<Integer> QueryPhotoStatisticalData(HttpServletRequest request) {
        int count = photographService.QueryPhotoStatisticalData();
        return new DataJsonResult<>(true, count);
    }

    /**
     * 查询评论总数统计数据
     */
    @RequestMapping(value = "/api/photograph/statistics/comments", method = RequestMethod.GET)
    @RestCheckAuthentication(name = "photograph")
    public DataJsonResult<Integer> QueryCommentStatisticalData(HttpServletRequest request) {
        int count = photographService.QueryCommentStatisticalData();
        return new DataJsonResult<>(true, count);
    }

    /**
     * 查询点赞总数统计数据
     */
    @RequestMapping(value = "/api/photograph/statistics/likes", method = RequestMethod.GET)
    @RestCheckAuthentication(name = "photograph")
    public DataJsonResult<Integer> QueryLikeStatisticalData(HttpServletRequest request) {
        int count = photographService.QueryLikeStatisticalData();
        return new DataJsonResult<>(true, count);
    }

    /**
     * 分页查询照片信息列表
     *
     * @param start
     * @param size
     * @return
     */
    @RequestMapping(value = "/api/photograph/type/{type}/start/{start}/size/{size}", method = RequestMethod.GET)
    @RestCheckAuthentication(name = "photograph")
    public DataJsonResult<List<Photograph>> QueryPhotographList(HttpServletRequest request
            , @Validated @NotNull @Min(0) @Max(1) @PathVariable("type") int type
            , @PathVariable("start") int start, @PathVariable("size") int size) throws WsgException {
        String username = (String) request.getSession().getAttribute("username");
        List<Photograph> photographList = photographService.QueryPhotographList(start, size, type, username);
        return new DataJsonResult<>(true, photographList);
    }

    /**
     * 获取照片信息图片
     *
     * @param id
     * @param index
     * @return
     */
    @RequestMapping(value = "/api/photograph/id/{id}/index/{index}/image", method = RequestMethod.GET)
    @RestCheckAuthentication(name = "photograph")
    public DataJsonResult<String> GetPhotographItemImage(HttpServletRequest request
            , @PathVariable("id") int id, @PathVariable("index") int index) {
        String url = photographService.GetPhotographItemPictureURL(id, index);
        return new DataJsonResult<>(true, url);
    }

    /**
     * 查询照片信息评论数量
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/api/photograph/id/{id}/comment", method = RequestMethod.GET)
    @RestCheckAuthentication(name = "photograph")
    public DataJsonResult<List<PhotographComment>> QueryPhotographCommentList(HttpServletRequest request
            , @PathVariable("id") int id) {
        List<PhotographComment> photographCommentList = photographService.QueryPhotographCommentList(id);
        return new DataJsonResult<>(true, photographCommentList);
    }

    /**
     * 添加照片信息评论
     *
     * @param request
     * @param comment
     * @return
     */
    @RequestMapping(value = "/api/photograph/id/{id}/comment", method = RequestMethod.POST)
    @RestCheckAuthentication(name = "photograph")
    public JsonResult AddPhotographComment(HttpServletRequest request, @PathVariable("id") Integer id
            , @Validated @NotBlank @Length(min = 1, max = 50) String comment) throws WsgException {
        String username = (String) request.getSession().getAttribute("username");
        photographService.AddPhotographComment(id, comment, username);
        return new JsonResult(true);
    }

    /**
     * 添加照片信息记录
     *
     * @param request
     * @param photograph
     * @param image1
     * @param image2
     * @param image3
     * @param image4
     * @return
     * @throws WsgException
     * @throws IOException
     */
    @RequestMapping(value = "/api/photograph", method = RequestMethod.POST)
    @RestCheckAuthentication(name = "photograph")
    public JsonResult AddPhotograph(HttpServletRequest request, @Validated Photograph photograph
            , MultipartFile image1, MultipartFile image2, MultipartFile image3, MultipartFile image4) throws WsgException, IOException {
        String username = (String) request.getSession().getAttribute("username");
        //插入照片信息记录
        int id = photographService.AddPhotograph(photograph.getTitle(), photograph.getContent()
                , photograph.getCount(), photograph.getType(), username);
        //上传照片图片
        photographService.UploadPhotographItemPicture(id, 1, image1.getInputStream());
        if (image2 != null && image2.getSize() > 0 && image2.getSize() < MAX_PICTURE_SIZE) {
            photographService.UploadPhotographItemPicture(id, 2, image2.getInputStream());
            if (image3 != null && image3.getSize() > 0 && image3.getSize() < MAX_PICTURE_SIZE) {
                photographService.UploadPhotographItemPicture(id, 3, image3.getInputStream());
                if (image4 != null && image4.getSize() > 0 && image4.getSize() < MAX_PICTURE_SIZE) {
                    photographService.UploadPhotographItemPicture(id, 4, image4.getInputStream());
                }
            }
        }
        return new JsonResult(true);
    }

    /**
     * 点赞照片信息
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/api/photograph/id/{id}/like", method = RequestMethod.POST)
    @RestCheckAuthentication(name = "photograph")
    public JsonResult LikePhotograph(HttpServletRequest request, @PathVariable("id") int id) {
        String username = (String) request.getSession().getAttribute("username");
        photographService.LikePhotograph(id, username);
        return new JsonResult(true);
    }

}
