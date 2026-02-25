package cn.gdeiassistant.common.tools.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageEncodeUtils {

    private static final Logger logger = LoggerFactory.getLogger(ImageEncodeUtils.class);

    /**
     * 图片格式类型枚举
     */
    public enum ImageFormTypeEnum {

        JPG("jpg"), GIF("gif"), PNG("png"), BMP("bmp");

        ImageFormTypeEnum(String imageType) {
            this.imageType = imageType;
        }

        String imageType;

        public String getImageType() {
            return imageType;
        }
    }

    /**
     * 将图片输入流转换为base64编码
     *
     * @param inputStream
     * @return
     */
    public static String convertToBase64(InputStream inputStream) {
        byte[] data = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = null;
        int len;
        String result = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
            result = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            logger.error("将图片流转换为 Base64 编码失败", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("关闭图片输入流失败", e);
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    logger.error("关闭图片输出流失败", e);
                }
            }
        }
        return result;
    }

    /**
     * 将图片输入流转换为指定图片格式的base64编码
     *
     * @param inputStream
     * @param imageFormTypeEnum
     * @return
     */
    public static String convertToBase64(InputStream inputStream, ImageFormTypeEnum imageFormTypeEnum) {
        String type = imageFormTypeEnum.imageType;
        String result = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedImage bufferedImage = ImageIO.read(inputStream);
            int width = bufferedImage.getWidth(null);
            int height = bufferedImage.getHeight(null);
            BufferedImage newBufferedImage = new BufferedImage(width, height,
                    BufferedImage.SCALE_SMOOTH);
            newBufferedImage.getGraphics().drawImage(bufferedImage, 0, 0, width, height, null);
            ImageIO.write(newBufferedImage, type, byteArrayOutputStream);
            result = Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            logger.error("按指定格式转换图片为 Base64 编码失败，type={}", type, e);
        } finally {
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    logger.error("关闭图片输出流失败", e);
                }
            }
        }
        return result;
    }

}
