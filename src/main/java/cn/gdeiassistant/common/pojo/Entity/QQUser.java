package cn.gdeiassistant.common.pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class QQUser extends ConnectUser {

}
