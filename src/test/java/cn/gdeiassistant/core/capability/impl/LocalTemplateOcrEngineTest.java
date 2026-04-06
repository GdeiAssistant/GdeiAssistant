package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalTemplateOcrEngineTest {

    private final LocalTemplateOcrEngine localTemplateOcrEngine = new LocalTemplateOcrEngine();

    @Test
    void shouldRecognizeAlphaNumericCaptchaLocally() throws Exception {
        String imageBase64 = renderImageBase64("A7B3", 120, 40, new Font(Font.MONOSPACED, Font.BOLD, 28), 4, false);

        assertEquals("A7B3", localTemplateOcrEngine.recognizeCaptcha(imageBase64, CheckCodeTypeEnum.ENGLISH_WITH_NUMBER, 4));
    }

    @Test
    void shouldRecognizeDigitStripLocally() throws Exception {
        String imageBase64 = renderImageBase64("0123456789", 280, 48, new Font(Font.SANS_SERIF, Font.BOLD, 28), 6, false);

        assertEquals("0123456789", localTemplateOcrEngine.recognizeNumbers(imageBase64));
    }

    private String renderImageBase64(String text, int width, int height, Font font, int spacing, boolean addNoise) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.setColor(Color.BLACK);
            graphics.setFont(font);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            FontMetrics metrics = graphics.getFontMetrics(font);
            int baseline = (height - metrics.getHeight()) / 2 + metrics.getAscent();
            int x = 8;
            for (int i = 0; i < text.length(); i++) {
                String ch = String.valueOf(text.charAt(i));
                graphics.drawString(ch, x, baseline);
                x += metrics.stringWidth(ch) + spacing;
            }

            if (addNoise) {
                graphics.setColor(new Color(70, 70, 70));
                graphics.drawLine(4, height - 8, width - 6, 6);
                graphics.drawRect(width / 3, 4, 1, 1);
                graphics.drawRect(width / 2, height - 10, 1, 1);
            }
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }
}
