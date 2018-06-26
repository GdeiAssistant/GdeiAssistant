package com.linguancheng.gdeiassistant.Controller.Upgrade;

import com.linguancheng.gdeiassistant.Pojo.Update.Android.AndroidUpdateInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UpdateController {

    @Autowired
    private AndroidUpdateInfo androidUpdateInfo;

    @RequestMapping(value = "/rest/update/android", method = RequestMethod.GET)
    @ResponseBody
    public AndroidUpdateInfo GetAndroidUpdateInformation() {
        return androidUpdateInfo;
    }
}
