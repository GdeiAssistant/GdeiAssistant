package com.linguancheng.gdeiassistant.Tools;

import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageEncodeUtils {

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
    public static String ConvertToBase64(InputStream inputStream) {
        byte[] data = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = null;
        int len;
        String result = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
            result = new BASE64Encoder().encode(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
    public static String ConvertToBase64(InputStream inputStream, ImageFormTypeEnum imageFormTypeEnum) {
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
            result = new BASE64Encoder().encode(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
