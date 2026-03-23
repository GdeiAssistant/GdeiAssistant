package cn.gdeiassistant.core.userProfile.controller.request;

public class HometownUpdateRequest {
    private String region;
    private String state;
    private String city;
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
