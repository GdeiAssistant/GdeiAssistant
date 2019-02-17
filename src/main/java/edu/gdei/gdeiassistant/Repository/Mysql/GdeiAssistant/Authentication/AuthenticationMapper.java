package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Authentication;

import edu.gdei.gdeiassistant.Pojo.Entity.Authentication;

public interface AuthenticationMapper {

    public Authentication selectAuthentication(String username);

    public void insertAuthentication(Authentication authentication);

    public void updateAuthentication(Authentication authentication);

    public void deleteAuthentication(String username);
}
