package edu.gdei.gdeiassistant.Pojo.Update.Android;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AndroidUpdateInfo {

    private String downloadURL;

    private String versionInfo;

    private String versionCodeName;

    private String fileSize;

    private int versionCode;

    @Value("#{propertiesReader['update.android.downloadURL']}")
    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    @Value("#{propertiesReader['update.android.versionInfo']}")
    public void setVersionInfo(String versionInfo) {
        this.versionInfo = versionInfo;
    }

    @Value("#{propertiesReader['update.android.versionCodeName']}")
    public void setVersionCodeName(String versionCodeName) {
        this.versionCodeName = versionCodeName;
    }

    @Value("#{propertiesReader['update.android.fileSize']}")
    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @Value("#{T(java.lang.Integer).valueOf(propertiesReader['update.android.versionCode'])}")
    public void setVersionCode(String versionCode) {
        this.versionCode = Integer.valueOf(versionCode);
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public String getVersionInfo() {
        return versionInfo;
    }

    public String getVersionCodeName() {
        return versionCodeName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public int getVersionCode() {
        return versionCode;
    }
}
