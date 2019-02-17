package edu.gdei.gdeiassistant.Pojo.CollectionQuery;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;

public class CollectionQuery {

    @Min(1)
    private Integer page;

    @NotBlank(message = "查询的图书不能为空")
    private String bookname;

    public String getBookname() {
        return bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
