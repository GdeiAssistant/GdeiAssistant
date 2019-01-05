package com.linguancheng.gdeiassistant.Controller.Upgrade;

import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Update.Android.AndroidUpdateInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateRestController {

    @Autowired
    private AndroidUpdateInfo androidUpdateInfo;

    @RequestMapping(value = "/rest/update/android", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<AndroidUpdateInfo> GetAndroidUpdateInformation() {
        return new DataJsonResult<>(true, androidUpdateInfo);
    }
}
