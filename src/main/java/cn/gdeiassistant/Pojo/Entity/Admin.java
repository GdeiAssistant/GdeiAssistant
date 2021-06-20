package cn.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Admin implements Serializable,Entity {

    private Boolean grayscale;

    private Boolean prideThemeLogo;

    private Boolean pinkThemeLogo;

    private Boolean authenticationForce;

    private Boolean authenticationErshou;

    private Boolean authenticationLostAndFound;

    private Boolean authenticationSecret;

    private Boolean authenticationPhotograph;

    private Boolean authenticationDelivery;

    private Boolean authenticationExpress;

    private Boolean authenticationTopic;

    public Boolean getGrayscale() {
        return grayscale;
    }

    public void setGrayscale(Boolean grayscale) {
        this.grayscale = grayscale;
    }

    public Boolean getPrideThemeLogo() {
        return prideThemeLogo;
    }

    public void setPrideThemeLogo(Boolean prideThemeLogo) {
        this.prideThemeLogo = prideThemeLogo;
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

    public Boolean getAuthenticationTopic() {
        return authenticationTopic;
    }

    public void setAuthenticationTopic(Boolean authenticationTopic) {
        this.authenticationTopic = authenticationTopic;
    }

    public Boolean getPinkThemeLogo() {
        return pinkThemeLogo;
    }

    public void setPinkThemeLogo(Boolean pinkThemeLogo) {
        this.pinkThemeLogo = pinkThemeLogo;
    }
}
