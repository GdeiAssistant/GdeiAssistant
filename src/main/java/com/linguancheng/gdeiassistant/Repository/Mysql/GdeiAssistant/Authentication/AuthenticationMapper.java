package com.gdeiassistant.gdeiassistant.Repository.Mysql.GdeiAssistant.Authentication;

import com.gdeiassistant.gdeiassistant.Pojo.Entity.Authentication;

public interface AuthenticationMapper {

    public Authentication selectAuthentication(String username);

    public void insertAuthentication(Authentication authentication);

    public void updateAuthentication(Authentication authentication);

    public void deleteAuthentication(String username);
}
