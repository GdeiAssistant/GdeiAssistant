package cn.gdeiassistant.Service.Photograph;

import cn.gdeiassistant.Pojo.Config.OSSConfig;
import cn.gdeiassistant.Pojo.Entity.Photograph;
import cn.gdeiassistant.Pojo.Entity.PhotographComment;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Photograph.PhotographMapper;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import com.aliyun.oss.OSSClient;
import com.taobao.wsgsvr.WsgException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Service
public class PhotographService {

    @Autowired
    private PhotographMapper photographMapper;

    @Autowired
    private OSSConfig ossConfig;

    /**
     * 查询照片统计数量
     *
     * @return
     */
    public int QueryPhotoStatisticalData() {
        return photographMapper.selectPhotographImageCount();
    }

    /**
     * 查询评论统计数量
     *
     * @return
     */
    public int QueryCommentStatisticalData() {
        return photographMapper.selectPhotographCommentCount();
    }

    /**
     * 查询点赞统计数量
     *
     * @return
     */
    public int QueryLikeStatisticalData() {
        return photographMapper.selectPhotographLikeCount();
    }

    /**
     * 分页查询照片信息列表
     *
     * @param start
     * @param size
     * @param type
     * @param username
     * @return
     */
    public List<Photograph> QueryPhotographList(int start, int size, int type, String username) throws WsgException {
        List<Photograph> photographList = photographMapper.selectPhotograph(start, size, type
                , StringEncryptUtils.encryptString(username));
        //清除空行
        photographList.removeIf(photograph -> photograph.getId() == null);
        for (Photograph photograph : photographList) {
            photograph.setUsername(StringEncryptUtils.decryptString(photograph.getUsername()));
        }
        return photographList;
    }

    /**
     * 查询照片信息评论列表
     *
     * @param id
     * @return
     */
    public List<PhotographComment> QueryPhotographCommentList(int id) throws WsgException {
        List<PhotographComment> commentList = photographMapper.selectPhotographCommentByPhotoId(id);
        for (PhotographComment photographComment : commentList) {
            photographComment.setUsername(StringEncryptUtils.decryptString(photographComment.getUsername()));
        }
        return commentList;
    }

    /**
     * 添加照片信息
     *
     * @param title
     * @param content
     * @param count
     * @param type
     * @param username
     */
    public int AddPhotograph(String title, String content, int count, int type, String username) throws WsgException {
        Photograph photograph = new Photograph();
        photograph.setTitle(title);
        photograph.setContent(content);
        photograph.setCount(count);
        photograph.setType(type);
        photograph.setUsername(StringEncryptUtils.encryptString(username));
        photographMapper.insertPhotograph(photograph);
        return photograph.getId();
    }

    /**
     * 添加照片信息评论
     *
     * @param id
     * @param comment
     * @param username
     * @throws WsgException
     */
    public void AddPhotographComment(int id, String comment, String username) throws WsgException {
        PhotographComment photographComment = new PhotographComment();
        photographComment.setPhotoId(id);
        photographComment.setComment(comment);
        photographComment.setUsername(StringEncryptUtils.encryptString(username));
        photographMapper.insertPhotographComment(photographComment);
    }

    /**
     * 上传照片信息图片
     *
     * @param id
     * @param index
     * @param inputStream
     * @return
     */
    public void UploadPhotographItemPicture(int id, int index, InputStream inputStream) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(ossConfig.getEndpoint(), ossConfig.getAccessKeyID(), ossConfig.getAccessKeySecret());
        //上传文件
        ossClient.putObject("gdeiassistant-userdata", "photograph/" + id + "_" + index + ".jpg", inputStream);
        ossClient.shutdown();
    }

    /**
     * 获取照片信息图片
     *
     * @param id
     * @param index
     * @return
     */
    public String GetPhotographItemPictureURL(int id, int index) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(ossConfig.getEndpoint(), ossConfig.getAccessKeyID(), ossConfig.getAccessKeySecret());
        String url = null;
        if (ossClient.doesObjectExist("gdeiassistant-userdata", "photograph/" + id + "_" + index + ".jpg")) {
            //设置过期时间30分钟
            Date expiration = new Date(new Date().getTime() + 1000 * 60 * 30);
            // 生成URL
            url = ossClient.generatePresignedUrl("gdeiassistant-userdata", "photograph/" + id + "_" + index + ".jpg", expiration).toString().replace("http", "https");
        }
        ossClient.shutdown();
        return url;
    }

    /**
     * 点赞照片信息
     *
     * @param id
     * @param username
     */
    public void LikePhotograph(int id, String username) throws WsgException {
        int count = photographMapper.selectPhotographLikeCountByPhotoIdAndUsername(id, StringEncryptUtils.encryptString(username));
        if (count == 0) {
            photographMapper.insertPhotographLike(id, StringEncryptUtils.encryptString(username));
        }
    }

}
