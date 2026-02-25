package cn.gdeiassistant.core.photograph.controller;

import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
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
            , @Validated @NotBlank @Length(min = 1, max = 50) String comment) {
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
            , MultipartFile image1, MultipartFile image2, MultipartFile image3, MultipartFile image4) throws IOException {
        String sessionId = (String) request.getAttribute("sessionId");
        int count = dto.getCount() != null ? dto.getCount() : 0;
        if (image1 == null || image1.isEmpty()) {
            count = 0;
        } else {
            if (count < 1) count = 1;
        }
        PhotographPublishDTO actual = new PhotographPublishDTO();
        actual.setTitle(dto.getTitle());
        actual.setContent(dto.getContent());
        actual.setCount(count);
        actual.setType(dto.getType());
        int id = photographService.addPhotograph(actual, sessionId);
        if (image1 != null && !image1.isEmpty() && image1.getSize() > 0 && image1.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
            photographService.uploadPhotographItemPicture(id, 1, image1.getInputStream());
            if (image2 != null && image2.getSize() > 0 && image2.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                photographService.uploadPhotographItemPicture(id, 2, image2.getInputStream());
                if (image3 != null && image3.getSize() > 0 && image3.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                    photographService.uploadPhotographItemPicture(id, 3, image3.getInputStream());
                    if (image4 != null && image4.getSize() > 0 && image4.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                        photographService.uploadPhotographItemPicture(id, 4, image4.getInputStream());
                    }
                }
            }
        }
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/photograph/id/{id}/like", method = RequestMethod.POST)
    public JsonResult LikePhotograph(HttpServletRequest request, @PathVariable("id") int id) {
        photographService.LikePhotograph(id, (String) request.getAttribute("sessionId"));
        return new JsonResult(true);
    }
}
