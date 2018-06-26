package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Log implements Serializable {

    private Integer id;

    private Date time;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);
    }

    public void setTime(String time) throws ParseException {
        this.time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
    }
}
