package com.linguancheng.gdeiassistant.Pojo.CollectionQuery;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CollectionQuery {

    @NotNull(message = "查询的页数不能为空")
    @Min(0)
    private int page;

    @NotBlank(message = "查询的图书不能为空")
    private String bookname;

    public String getBookname() {
        return bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
