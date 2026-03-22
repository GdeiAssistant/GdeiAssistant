package cn.gdeiassistant.core.photograph.controller;

import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.photograph.pojo.dto.PhotographPublishDTO;
import cn.gdeiassistant.core.photograph.pojo.vo.PhotographCommentVO;
import cn.gdeiassistant.core.photograph.pojo.vo.PhotographVO;
import cn.gdeiassistant.core.photograph.service.PhotographService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

@RestController
public class PhotographController {

    @Autowired
    private PhotographService photographService;

    @RequestMapping(value = "/api/photograph/statistics/photos", method = RequestMethod.GET)
    public DataJsonResult<Integer> queryPhotoStatisticalData(HttpServletRequest request) {
        int count = photographService.queryPhotoStatisticalData();
        return new DataJsonResult<>(true, count);
    }

    @RequestMapping(value = "/api/photograph/statistics/comments", method = RequestMethod.GET)
    public DataJsonResult<Integer> queryCommentStatisticalData(HttpServletRequest request) {
        int count = photographService.queryCommentStatisticalData();
        return new DataJsonResult<>(true, count);
    }

    @RequestMapping(value = "/api/photograph/statistics/likes", method = RequestMethod.GET)
    public DataJsonResult<Integer> queryLikeStatisticalData(HttpServletRequest request) {
        int count = photographService.queryLikeStatisticalData();
        return new DataJsonResult<>(true, count);
    }

    @RequestMapping(value = "/api/photograph/type/{type}/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<PhotographVO>> queryPhotographList(HttpServletRequest request
            , @Validated @NotNull @Min(0) @Max(1) @PathVariable("type") int type
            , @PathVariable("start") int start, @PathVariable("size") int size) {
        List<PhotographVO> list = photographService.queryPhotographList(start, size, type, (String) request.getAttribute("sessionId"));
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/photograph/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<PhotographVO> getPhotographDetail(HttpServletRequest request, @PathVariable("id") int id) throws DataNotExistException {
        String sessionId = (String) request.getAttribute("sessionId");
        PhotographVO vo = photographService.getPhotographById(id, sessionId);
        return new DataJsonResult<>(true, vo);
    }

    @RequestMapping(value = "/api/photograph/id/{id}/index/{index}/image", method = RequestMethod.GET)
    public DataJsonResult<String> getPhotographItemImage(HttpServletRequest request
            , @PathVariable("id") int id, @PathVariable("index") int index) {
        String url = photographService.getPhotographItemPictureURL(id, index);
        return new DataJsonResult<>(true, url);
    }

    @RequestMapping(value = "/api/photograph/id/{id}/comment", method = RequestMethod.GET)
    public DataJsonResult<List<PhotographCommentVO>> queryPhotographCommentList(HttpServletRequest request
            , @PathVariable("id") int id) {
        List<PhotographCommentVO> list = photographService.queryPhotographCommentList(id);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/photograph/id/{id}/comment", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addPhotographComment(HttpServletRequest request, @PathVariable("id") Integer id
            , @Validated @NotBlank @Length(min = 1, max = 50) String comment) throws DataNotExistException {
        photographService.addPhotographComment(id, comment, (String) request.getAttribute("sessionId"));
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/photograph/profile/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<PhotographVO>> getMyPhotographs(HttpServletRequest request
            , @PathVariable("start") int start, @PathVariable("size") int size) {
        String sessionId = (String) request.getAttribute("sessionId");
        List<PhotographVO> list = photographService.queryMyPhotographList(sessionId, start, size);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/photograph", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addPhotograph(HttpServletRequest request, @Validated PhotographPublishDTO dto
            , MultipartFile image1, MultipartFile image2, MultipartFile image3, MultipartFile image4
            , @RequestParam(value = "imageKeys", required = false) String[] imageKeys) throws IOException {
        String sessionId = (String) request.getAttribute("sessionId");
        MultipartFile[] images = new MultipartFile[]{image1, image2, image3, image4};
        int uploadedFileCount = 0;
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty() && image.getSize() > 0 && image.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                uploadedFileCount++;
            }
        }
        int uploadedKeyCount = 0;
        if (imageKeys != null) {
            for (String imageKey : imageKeys) {
                if (StringUtils.isNotBlank(imageKey)) {
                    uploadedKeyCount++;
                } else {
                    return new JsonResult(false, "不合法的图片文件");
                }
            }
        }
        int count = uploadedKeyCount > 0 ? uploadedKeyCount : uploadedFileCount;
        if (count <= 0) {
            return new JsonResult(false, "不合法的图片文件");
        }
        if (count > 4) {
            return new JsonResult(false, "不合法的图片文件");
        }
        if (uploadedKeyCount > 0 && uploadedFileCount > 0) {
            return new JsonResult(false, "不支持混合上传图片参数");
        }
        if (uploadedFileCount == 0) {
            dto.setCount(count);
        } else {
            if (count < 1) {
                count = 1;
            }
        }
        PhotographPublishDTO actual = new PhotographPublishDTO();
        actual.setTitle(dto.getTitle());
        actual.setContent(dto.getContent());
        actual.setCount(count);
        actual.setType(dto.getType());
        int id = photographService.addPhotograph(actual, sessionId);
        try {
            if (uploadedFileCount > 0) {
                int imageIndex = 1;
                for (MultipartFile image : images) {
                    if (image != null && !image.isEmpty() && image.getSize() > 0 && image.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                        photographService.uploadPhotographItemPicture(id, imageIndex++, image.getInputStream());
                    }
                }
            } else if (uploadedKeyCount > 0) {
                for (int i = 1; i <= imageKeys.length; i++) {
                    photographService.movePhotographItemPictureFromTempObject(id, i, imageKeys[i - 1]);
                }
            }
        } catch (Exception e) {
            photographService.deletePhotograph(id);
            return new JsonResult(false, "拍好校园图片上传失败");
        }
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/photograph/id/{id}/like", method = RequestMethod.POST)
    public JsonResult LikePhotograph(HttpServletRequest request, @PathVariable("id") int id) throws DataNotExistException {
        photographService.LikePhotograph(id, (String) request.getAttribute("sessionId"));
        return new JsonResult(true);
    }
}
