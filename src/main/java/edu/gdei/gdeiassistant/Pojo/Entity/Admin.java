package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Admin implements Serializable,Entity {

    private Boolean grayscale;

    private Boolean pridetheme;

    private Boolean authenticationForce;

    private Boolean authenticationErshou;

    private Boolean authenticationLostAndFound;

    private Boolean authenticationSecret;

    private Boolean authenticationPhotograph;

    private Boolean authenticationDelivery;

    private Boolean authenticationExpress;

    public Boolean getGrayscale() {
        return grayscale;
    }

    public void setGrayscale(Boolean grayscale) {
        this.grayscale = grayscale;
    }

    public Boolean getPridetheme() {
        return pridetheme;
    }

    public void setPridetheme(Boolean pridetheme) {
        this.pridetheme = pridetheme;
    }

    public Boolean getAuthenticationForce() {
        return authenticationForce;
    }

    public void setAuthenticationForce(Boolean authenticationForce) {
        this.authenticationForce = authenticationForce;
    }

    public Boolean getAuthenticationErshou() {
        return authenticationErshou;
    }

    public void setAuthenticationErshou(Boolean authenticationErshou) {
        this.authenticationErshou = authenticationErshou;
    }

    public Boolean getAuthenticationLostAndFound() {
        return authenticationLostAndFound;
    }

    public void setAuthenticationLostAndFound(Boolean authenticationLostAndFound) {
        this.authenticationLostAndFound = authenticationLostAndFound;
    }

    public Boolean getAuthenticationSecret() {
        return authenticationSecret;
    }

    public void setAuthenticationSecret(Boolean authenticationSecret) {
        this.authenticationSecret = authenticationSecret;
    }

    public Boolean getAuthenticationPhotograph() {
        return authenticationPhotograph;
    }

    public void setAuthenticationPhotograph(Boolean authenticationPhotograph) {
        this.authenticationPhotograph = authenticationPhotograph;
    }

    public Boolean getAuthenticationExpress() {
        return authenticationExpress;
    }

    public void setAuthenticationExpress(Boolean authenticationExpress) {
        this.authenticationExpress = authenticationExpress;
    }

    public Boolean getAuthenticationDelivery() {
        return authenticationDelivery;
    }

    public void setAuthenticationDelivery(Boolean authenticationDelivery) {
        this.authenticationDelivery = authenticationDelivery;
    }
}
