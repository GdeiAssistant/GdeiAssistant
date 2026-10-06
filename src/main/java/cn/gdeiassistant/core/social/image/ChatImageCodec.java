package cn.gdeiassistant.core.social.image;

import cn.gdeiassistant.core.social.exception.SocialException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Locale;

/**
 * 私信图片编解码：签名识别 → 先读宽高再 decode → 重编码去元数据 → SHA-256。
 * 在像素限制检查前不调用 reader.read，避免为超大图分配 bitmap。
 */
public final class ChatImageCodec {

    public static final int MAX_BYTES = 5 * 1024 * 1024;
    public static final int MAX_EDGE = 4096;
    public static final long MAX_PIXELS = 16_000_000L;

    public static final String MIME_JPEG = "image/jpeg";
    public static final String MIME_PNG = "image/png";

    private ChatImageCodec() {
    }

    public static final class CanonicalImage {
        public final byte[] bytes;
        public final String contentType;
        public final int width;
        public final int height;
        public final String sha256Hex;

        public CanonicalImage(byte[] bytes, String contentType, int width, int height, String sha256Hex) {
            this.bytes = bytes;
            this.contentType = contentType;
            this.width = width;
            this.height = height;
            this.sha256Hex = sha256Hex;
        }

        public int size() {
            return bytes.length;
        }
    }

    public static CanonicalImage canonicalize(byte[] raw, String claimedContentType) {
        if (raw == null || raw.length == 0) {
            throw SocialException.invalidRequest("图片不能为空");
        }
        if (raw.length > MAX_BYTES) {
            throw SocialException.invalidRequest("图片不能超过 5 MiB");
        }
        DetectedFormat detected = detectFormat(raw);
        if (detected == null) {
            throw SocialException.invalidRequest("仅支持 JPEG/PNG 图片");
        }
        rejectForgedMime(claimedContentType, detected.contentType);

        ImageReader reader = null;
        try {
            reader = createReader(detected.formatName);
            try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(raw))) {
                if (input == null) {
                    throw SocialException.invalidRequest("无法读取图片");
                }
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                validateDimensions(width, height);
                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw SocialException.invalidRequest("无法解码图片");
                }
                if (MIME_JPEG.equals(detected.contentType)) {
                    image = orientPixels(image, jpegOrientation(raw));
                    width = image.getWidth();
                    height = image.getHeight();
                }
                byte[] encoded = reencode(image, detected.formatName);
                if (encoded.length > MAX_BYTES) {
                    throw SocialException.invalidRequest("规范化后图片不能超过 5 MiB");
                }
                return new CanonicalImage(encoded, detected.contentType, width, height, sha256Hex(encoded));
            }
        } catch (SocialException ex) {
            throw ex;
        } catch (IOException ex) {
            throw SocialException.invalidRequest("图片解码失败");
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }

    private static void validateDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw SocialException.invalidRequest("图片尺寸无效");
        }
        if (width > MAX_EDGE || height > MAX_EDGE) {
            throw SocialException.invalidRequest("图片边长不能超过 4096");
        }
        if ((long) width * (long) height > MAX_PIXELS) {
            throw SocialException.invalidRequest("图片像素不能超过 1600 万");
        }
    }

    private static void rejectForgedMime(String claimedContentType, String actual) {
        if (claimedContentType == null || claimedContentType.isBlank()) {
            return;
        }
        String claimed = claimedContentType.trim().toLowerCase(Locale.ROOT);
        int semi = claimed.indexOf(';');
        if (semi >= 0) {
            claimed = claimed.substring(0, semi).trim();
        }
        if ("application/octet-stream".equals(claimed) || "*/*".equals(claimed)) {
            return;
        }
        if (!actual.equals(claimed)) {
            throw SocialException.invalidRequest("图片 MIME 与实际格式不一致");
        }
    }

    private static DetectedFormat detectFormat(byte[] raw) {
        if (looksLikeSvg(raw)) {
            throw SocialException.invalidRequest("不支持 SVG 图片");
        }
        if (raw.length >= 6 && raw[0] == 'G' && raw[1] == 'I' && raw[2] == 'F'
                && raw[3] == '8' && (raw[4] == '7' || raw[4] == '9') && raw[5] == 'a') {
            throw SocialException.invalidRequest("不支持 GIF 图片");
        }
        if (raw.length >= 12
                && raw[0] == 'R' && raw[1] == 'I' && raw[2] == 'F' && raw[3] == 'F'
                && raw[8] == 'W' && raw[9] == 'E' && raw[10] == 'B' && raw[11] == 'P') {
            throw SocialException.invalidRequest("不支持 WebP 图片");
        }
        if (raw.length >= 3
                && (raw[0] & 0xFF) == 0xFF && (raw[1] & 0xFF) == 0xD8 && (raw[2] & 0xFF) == 0xFF) {
            return new DetectedFormat("jpeg", MIME_JPEG);
        }
        if (raw.length >= 8
                && (raw[0] & 0xFF) == 0x89 && raw[1] == 0x50 && raw[2] == 0x4E && raw[3] == 0x47
                && raw[4] == 0x0D && raw[5] == 0x0A && raw[6] == 0x1A && raw[7] == 0x0A) {
            return new DetectedFormat("png", MIME_PNG);
        }
        return null;
    }

    private static boolean looksLikeSvg(byte[] raw) {
        int start = 0;
        while (start < raw.length && Character.isWhitespace((char) (raw[start] & 0xFF))) {
            start++;
        }
        int len = Math.min(raw.length - start, 256);
        if (len <= 0) {
            return false;
        }
        String head = new String(raw, start, len, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
        return head.startsWith("<?xml") && head.contains("<svg") || head.startsWith("<svg");
    }

    private static ImageReader createReader(String formatName) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(formatName);
        if (!readers.hasNext()) {
            throw SocialException.invalidRequest("当前环境不支持该图片格式");
        }
        return readers.next();
    }

    private static byte[] reencode(BufferedImage image, String formatName) throws IOException {
        BufferedImage rgb = image;
        if ("jpeg".equals(formatName) && image.getType() != BufferedImage.TYPE_INT_RGB
                && image.getColorModel().hasAlpha()) {
            rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = rgb.createGraphics();
            try { graphics.drawImage(image, 0, 0, java.awt.Color.WHITE, null); }
            finally { graphics.dispose(); }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
        if (!writers.hasNext()) {
            throw SocialException.invalidRequest("当前环境无法重编码图片");
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if ("jpeg".equals(formatName) && param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.92f);
            }
            writer.write(null, new javax.imageio.IIOImage(rgb, null, null), param);
        } finally {
            writer.dispose();
        }
        return out.toByteArray();
    }

    /** Read only EXIF's inline SHORT Orientation from bounded JPEG APP1 segments. */
    private static int jpegOrientation(byte[] raw) {
        int p = 2;
        while (p + 4 <= raw.length && (raw[p] & 255) == 255) {
            while (p < raw.length && (raw[p] & 255) == 255) p++;
            if (p >= raw.length) break;
            int marker = raw[p++] & 255;
            if (marker == 0xda || marker == 0xd9) break;
            if (marker == 1 || marker >= 0xd0 && marker <= 0xd7) continue;
            if (p + 2 > raw.length) break;
            int length = (raw[p] & 255) << 8 | raw[p + 1] & 255;
            if (length < 2 || length > raw.length - p) break;
            int start = p + 2, end = p + length;
            if (marker == 0xe1 && start <= raw.length - 14 && end - start >= 14
                    && raw[start] == 'E' && raw[start + 1] == 'x' && raw[start + 2] == 'i'
                    && raw[start + 3] == 'f' && raw[start + 4] == 0 && raw[start + 5] == 0) {
                ByteBuffer tiff = ByteBuffer.wrap(raw, start + 6, end - start - 6).slice();
                boolean little = tiff.get(0) == 'I' && tiff.get(1) == 'I';
                boolean big = tiff.get(0) == 'M' && tiff.get(1) == 'M';
                if (!little && !big) return 1;
                tiff.order(little ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
                if ((tiff.getShort(2) & 65535) != 42) return 1;
                long offset = Integer.toUnsignedLong(tiff.getInt(4));
                if (offset > tiff.limit() - 2L) return 1;
                int base = (int) offset, count = tiff.getShort(base) & 65535;
                if (count > (tiff.limit() - base - 2) / 12) return 1;
                for (int i = 0; i < count; i++) {
                    int entry = base + 2 + i * 12;
                    if ((tiff.getShort(entry) & 65535) == 0x0112
                            && (tiff.getShort(entry + 2) & 65535) == 3 && tiff.getInt(entry + 4) == 1) {
                        int value = tiff.getShort(entry + 8) & 65535;
                        return value >= 1 && value <= 8 ? value : 1;
                    }
                }
            }
            p = end;
        }
        return 1;
    }

    static BufferedImage orientPixels(BufferedImage image, int orientation) {
        int w = image.getWidth(), h = image.getHeight();
        AffineTransform transform = switch (orientation) {
            case 2 -> new AffineTransform(-1, 0, 0, 1, w, 0);
            case 3 -> new AffineTransform(-1, 0, 0, -1, w, h);
            case 4 -> new AffineTransform(1, 0, 0, -1, 0, h);
            case 5 -> new AffineTransform(0, 1, 1, 0, 0, 0);
            case 6 -> new AffineTransform(0, 1, -1, 0, h, 0);
            case 7 -> new AffineTransform(0, -1, -1, 0, h, w);
            case 8 -> new AffineTransform(0, -1, 1, 0, 0, w);
            default -> null;
        };
        if (transform == null) return image;
        BufferedImage oriented = new BufferedImage(orientation >= 5 ? h : w,
                orientation >= 5 ? w : h, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = oriented.createGraphics();
        try { graphics.drawImage(image, transform, null); }
        finally { graphics.dispose(); }
        return oriented;
    }

    public static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static final class DetectedFormat {
        final String formatName;
        final String contentType;

        DetectedFormat(String formatName, String contentType) {
            this.formatName = formatName;
            this.contentType = contentType;
        }
    }
}
