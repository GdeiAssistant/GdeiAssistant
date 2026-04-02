package cn.gdeiassistant.core.collectionquery.pojo.dto;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Min;
import java.io.Serializable;

/**
 * 馆藏检索查询条件 DTO。
 */
public class CollectionQueryDTO implements Serializable {

    @Min(1)
    private Integer page;

    @NotBlank(message = "查询的图书不能为空")
    private String bookname;

    public String getBookname() { return bookname; }
    public void setBookname(String bookname) { this.bookname = bookname; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
}
