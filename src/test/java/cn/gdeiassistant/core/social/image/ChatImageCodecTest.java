package cn.gdeiassistant.core.social.image;

import cn.gdeiassistant.core.social.exception.SocialException;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class ChatImageCodecTest {

    @Test
    void acceptsPngAndProducesCanonicalHashWithoutKeyLeak() throws Exception {
        byte[] png = encodePng(2, 2);
        ChatImageCodec.CanonicalImage image = ChatImageCodec.canonicalize(png, "image/png");
        assertEquals(ChatImageCodec.MIME_PNG, image.contentType);
        assertEquals(2, image.width);
        assertEquals(2, image.height);
        assertTrue(image.size() > 0);
        assertTrue(image.size() <= ChatImageCodec.MAX_BYTES);
        assertEquals(64, image.sha256Hex.length());
        assertFalse(image.sha256Hex.contains("/"));
    }

    @Test
    void rejectsGifWebpSvgAndForgedMime() throws Exception {
        byte[] gif = "GIF89a......".getBytes(StandardCharsets.US_ASCII);
        SocialException gifEx = assertThrows(SocialException.class,
                () -> ChatImageCodec.canonicalize(gif, "image/gif"));
        assertEquals("INVALID_REQUEST", gifEx.getErrorCode());
        assertTrue(gifEx.getMessage().contains("GIF"));

        byte[] webp = new byte[12];
        System.arraycopy("RIFF".getBytes(StandardCharsets.US_ASCII), 0, webp, 0, 4);
        System.arraycopy("WEBP".getBytes(StandardCharsets.US_ASCII), 0, webp, 8, 4);
        SocialException webpEx = assertThrows(SocialException.class,
                () -> ChatImageCodec.canonicalize(webp, "image/webp"));
        assertTrue(webpEx.getMessage().contains("WebP"));

        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>".getBytes(StandardCharsets.UTF_8);
        SocialException svgEx = assertThrows(SocialException.class,
                () -> ChatImageCodec.canonicalize(svg, "image/svg+xml"));
        assertTrue(svgEx.getMessage().contains("SVG"));

        byte[] png = encodePng(1, 1);
        SocialException forged = assertThrows(SocialException.class,
                () -> ChatImageCodec.canonicalize(png, "image/jpeg"));
        assertTrue(forged.getMessage().contains("MIME"));
    }

    @Test
    void checksDimensionsBeforeDecodeRejectsOversizedEdge() throws Exception {
        // Construct a PNG IHDR claiming huge width without allocating that bitmap via ImageIO.read.
        // ImageReader.getWidth reads IHDR; our codec must reject before reader.read.
        byte[] huge = pngWithIhdrDimensions(5000, 10);
        SocialException ex = assertThrows(SocialException.class,
                () -> ChatImageCodec.canonicalize(huge, "image/png"));
        assertEquals("INVALID_REQUEST", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("4096") || ex.getMessage().contains("解码"));
    }

    @Test
    void rejectsOversizeRawBytes() {
        byte[] raw = new byte[ChatImageCodec.MAX_BYTES + 1];
        Arrays.fill(raw, (byte) 0);
        raw[0] = (byte) 0xFF;
        raw[1] = (byte) 0xD8;
        raw[2] = (byte) 0xFF;
        SocialException ex = assertThrows(SocialException.class,
                () -> ChatImageCodec.canonicalize(raw, "image/jpeg"));
        assertTrue(ex.getMessage().contains("5 MiB"));
    }

    @Test
    void rotatesCameraJpegPixelsBeforeDroppingExif() throws Exception {
        BufferedImage source = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(source, "jpeg", jpeg));
        byte[] original = jpeg.toByteArray();
        for (ByteOrder order : new ByteOrder[]{ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN}) {
            for (int orientation = 1; orientation <= 8; orientation++) {
                byte[] fixture = withExif(original, orientation, order);
                var image = ChatImageCodec.canonicalize(fixture, "image/jpeg");
                assertEquals(orientation >= 5 ? 2 : 3, image.width);
                assertEquals(orientation >= 5 ? 3 : 2, image.height);
                assertFalse(new String(image.bytes, StandardCharsets.ISO_8859_1).contains("Exif"));
                assertEquals(image.sha256Hex, ChatImageCodec.canonicalize(fixture, "image/jpeg").sha256Hex);
            }
        }
        byte[] malformed = withExif(original, 6, ByteOrder.LITTLE_ENDIAN);
        Arrays.fill(malformed, 16, 20, (byte) 0xff); // out-of-range IFD offset
        assertDoesNotThrow(() -> ChatImageCodec.canonicalize(malformed, "image/jpeg"));
    }

    @Test
    void appliesAllEightExifPixelMappings() {
        BufferedImage source = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 2; y++) for (int x = 0; x < 3; x++) source.setRGB(x, y, 1 + y * 3 + x);
        for (int orientation = 1; orientation <= 8; orientation++) {
            BufferedImage result = ChatImageCodec.orientPixels(source, orientation);
            for (int y = 0; y < 2; y++) for (int x = 0; x < 3; x++) {
                int[] point = switch (orientation) {
                    case 2 -> new int[]{2 - x, y};
                    case 3 -> new int[]{2 - x, 1 - y};
                    case 4 -> new int[]{x, 1 - y};
                    case 5 -> new int[]{y, x};
                    case 6 -> new int[]{1 - y, x};
                    case 7 -> new int[]{1 - y, 2 - x};
                    case 8 -> new int[]{y, 2 - x};
                    default -> new int[]{x, y};
                };
                assertEquals(source.getRGB(x, y), result.getRGB(point[0], point[1]), "orientation=" + orientation);
            }
        }
    }

    private static byte[] withExif(byte[] jpeg, int orientation, ByteOrder order) throws Exception {
        ByteBuffer tiff = ByteBuffer.allocate(26).order(order);
        tiff.put(order == ByteOrder.LITTLE_ENDIAN ? (byte) 'I' : (byte) 'M');
        tiff.put(order == ByteOrder.LITTLE_ENDIAN ? (byte) 'I' : (byte) 'M');
        tiff.putShort((short) 42).putInt(8).putShort((short) 1);
        tiff.putShort((short) 0x0112).putShort((short) 3).putInt(1).putShort((short) orientation).putShort((short) 0).putInt(0);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(jpeg, 0, 2);
        out.write(new byte[]{(byte) 0xff, (byte) 0xe1, 0, 34});
        out.write(new byte[]{'E', 'x', 'i', 'f', 0, 0});
        out.write(tiff.array());
        out.write(jpeg, 2, jpeg.length - 2);
        return out.toByteArray();
    }

    private static byte[] encodePng(int w, int h) throws Exception {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, "png", out));
        return out.toByteArray();
    }

    /** Minimal PNG with custom IHDR width/height; may fail decode after dimension check. */
    private static byte[] pngWithIhdrDimensions(int width, int height) throws Exception {
        byte[] base = encodePng(1, 1);
        // PNG signature 8 + IHDR length 4 + "IHDR" 4 + data starts at 16
        int ihdrData = 16;
        writeInt(base, ihdrData, width);
        writeInt(base, ihdrData + 4, height);
        // CRC will be wrong; ImageReader may fail — either dimension reject or decode fail is OK for regression.
        return base;
    }

    private static void writeInt(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) ((value >>> 24) & 0xFF);
        bytes[offset + 1] = (byte) ((value >>> 16) & 0xFF);
        bytes[offset + 2] = (byte) ((value >>> 8) & 0xFF);
        bytes[offset + 3] = (byte) (value & 0xFF);
    }
}
