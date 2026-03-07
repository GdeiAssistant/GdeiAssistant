package cn.gdeiassistant.core.objectStorage.service;

import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UploadService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Autowired
    private R2StorageService r2StorageService;

    public Map<String, String> createPresignedUpload(String fileName, String contentType) {
        String normalizedContentType = contentType == null ? null : contentType.trim();
        String objectKey = buildObjectKey(fileName);
        String presignedUrl = r2StorageService.generatePutPresignedUrl(objectKey, normalizedContentType, 15, TimeUnit.MINUTES);
        Map<String, String> result = new LinkedHashMap<>(2);
        result.put("url", presignedUrl);
        result.put("objectKey", objectKey);
        return result;
    }

    private String buildObjectKey(String fileName) {
        String extension = extractExtension(fileName);
        return "upload/" + DATE_FORMATTER.format(LocalDate.now()) + "/"
                + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private String extractExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        String normalizedFileName = fileName.replace("\\", "/");
        int slashIndex = normalizedFileName.lastIndexOf('/');
        String baseName = slashIndex >= 0 ? normalizedFileName.substring(slashIndex + 1) : normalizedFileName;
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == baseName.length() - 1) {
            return "";
        }
        String extension = baseName.substring(dotIndex).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,16}") ? extension : "";
    }
}
