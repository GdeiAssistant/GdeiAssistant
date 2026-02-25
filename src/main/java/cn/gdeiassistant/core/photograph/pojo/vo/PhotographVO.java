package cn.gdeiassistant.core.photograph.pojo.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 照片列表/详情视图。
 */
public class PhotographVO implements Serializable {

    private Integer id;
    private String title;
    private String content;
    private Integer count;
    private Integer type;
    private String username;
    private Date createTime;
    private Integer likeCount;
    private Integer commentCount;
    private Integer liked;
    private String firstImageUrl;
    private List<String> imageUrls;
    private List<PhotographCommentVO> photographCommentList;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    public Integer getLiked() { return liked; }
    public void setLiked(Integer liked) { this.liked = liked; }
    public String getFirstImageUrl() { return firstImageUrl; }
    public void setFirstImageUrl(String firstImageUrl) { this.firstImageUrl = firstImageUrl; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public List<PhotographCommentVO> getPhotographCommentList() { return photographCommentList; }
    public void setPhotographCommentList(List<PhotographCommentVO> photographCommentList) { this.photographCommentList = photographCommentList; }
}
