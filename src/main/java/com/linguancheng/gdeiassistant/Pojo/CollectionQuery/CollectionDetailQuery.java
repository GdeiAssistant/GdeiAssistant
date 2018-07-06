package com.linguancheng.gdeiassistant.Pojo.CollectionQuery;

import org.hibernate.validator.constraints.NotBlank;

public class CollectionDetailQuery {

    @NotBlank
    private String opacUrl;

    @NotBlank
    private String search;

    @NotBlank
    private String schoolId;

    @NotBlank
    private String searchtype;

    @NotBlank
    private String page;

    @NotBlank
    private String xc;

    public String getOpacUrl() {
        return opacUrl;
    }

    public void setOpacUrl(String opacUrl) {
        this.opacUrl = opacUrl;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getSearchtype() {
        return searchtype;
    }

    public void setSearchtype(String searchtype) {
        this.searchtype = searchtype;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getXc() {
        return xc;
    }

    public void setXc(String xc) {
        this.xc = xc;
    }
}
