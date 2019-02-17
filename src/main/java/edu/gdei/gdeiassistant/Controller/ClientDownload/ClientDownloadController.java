package edu.gdei.gdeiassistant.Controller.ClientDownload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ClientDownloadController {

    private String androidDownloadURL;

    private String androidGooglePlayDownloadURL;

    private String androidAmazonDownloadURL;

    @Value("#{propertiesReader['update.android.gooleplayURL']}")
    public void setAndroidGooglePlayDownloadURL(String androidGooglePlayDownloadURL) {
        this.androidGooglePlayDownloadURL = androidGooglePlayDownloadURL;
    }

    @Value("#{propertiesReader['update.android.downloadURL']}")
    public void setAndroidDownloadURL(String androidDownloadURL) {
        this.androidDownloadURL = androidDownloadURL;
    }

    @Value("#{propertiesReader['update.android.amazonURL']}")
    public void setAndroidAmazonDownloadURL(String androidAmazonDownloadURL) {
        this.androidAmazonDownloadURL = androidAmazonDownloadURL;
    }

    @RequestMapping(value = "/download/android", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> GetAndroidDownloadURl() {
        Map<String, String> map = new HashMap<>();
        map.put("url", androidDownloadURL);
        return map;
    }

    @RequestMapping(value = "/download/android/googleplay", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> GetAndroidGooglePlayDownloadURL() {
        Map<String, String> map = new HashMap<>();
        map.put("url", androidGooglePlayDownloadURL);
        return map;
    }

    @RequestMapping(value = "/download/android/amazon", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> GetAndroidAmazonDownloadURL() {
        Map<String, String> map = new HashMap<>();
        map.put("url", androidAmazonDownloadURL);
        return map;
    }
}
