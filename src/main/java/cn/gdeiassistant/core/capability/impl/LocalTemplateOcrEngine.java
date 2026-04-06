package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class LocalTemplateOcrEngine {

    private static final String DIGITS = "0123456789";
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LETTERS_AND_DIGITS = LETTERS + DIGITS;

    private static final int TARGET_WIDTH = 24;
    private static final int TARGET_HEIGHT = 32;
    private static final int RENDER_WIDTH = 36;
    private static final int RENDER_HEIGHT = 44;

    private static final double MAX_ACCEPTED_SCORE = 0.39D;
    private static final double MIN_SCORE_MARGIN = 0.015D;

    private final Map<Character, List<boolean[][]>> digitTemplates = buildTemplates(DIGITS);
    private final Map<Character, List<boolean[][]>> alphaNumericTemplates = buildTemplates(LETTERS_AND_DIGITS);

    public String recognizeCaptcha(String imageBase64, CheckCodeTypeEnum typeEnum, int length) throws RecognitionException {
        String allowedCharacters = resolveAllowedCharacters(typeEnum);
        if (StringUtils.isBlank(allowedCharacters)) {
            throw new RecognitionException("本地OCR暂不支持当前验证码类型");
        }
        boolean[][] binaryImage = decodeToBinaryImage(imageBase64);
        List<boolean[][]> segments = splitCharacters(binaryImage, length);
        if (segments.isEmpty()) {
            throw new RecognitionException("本地OCR识别验证码失败");
        }
        if (length > 0 && segments.size() != length) {
            throw new RecognitionException("本地OCR识别验证码失败");
        }
        StringBuilder builder = new StringBuilder();
        for (boolean[][] segment : segments) {
            MatchResult matchResult = matchSegment(segment, allowedCharacters);
            if (!matchResult.accepted()) {
                throw new RecognitionException("本地OCR识别验证码失败");
            }
            builder.append(matchResult.getCharacter());
        }
        String result = builder.toString().toUpperCase(Locale.ROOT);
        if (length > 0 && result.length() != length) {
            throw new RecognitionException("本地OCR识别验证码失败");
        }
        return result;
    }

    public String recognizeNumbers(String imageBase64) throws RecognitionException {
        boolean[][] binaryImage = decodeToBinaryImage(imageBase64);
        List<boolean[][]> segments = splitCharacters(binaryImage, 0);
        if (segments.isEmpty()) {
            throw new RecognitionException("本地OCR数字识别失败");
        }
        StringBuilder builder = new StringBuilder();
        for (boolean[][] segment : segments) {
            MatchResult matchResult = matchSegment(segment, DIGITS);
            if (matchResult.accepted()) {
                builder.append(matchResult.getCharacter());
            }
        }
        String result = builder.toString();
        if (StringUtils.isBlank(result)) {
            throw new RecognitionException("本地OCR数字识别失败");
        }
        return result;
    }

    private String resolveAllowedCharacters(CheckCodeTypeEnum typeEnum) {
        if (typeEnum == CheckCodeTypeEnum.NUMBER) {
            return DIGITS;
        }
        if (typeEnum == CheckCodeTypeEnum.ENGLISH) {
            return LETTERS;
        }
        if (typeEnum == CheckCodeTypeEnum.ENGLISH_WITH_NUMBER) {
            return LETTERS_AND_DIGITS;
        }
        return "";
    }

    private boolean[][] decodeToBinaryImage(String imageBase64) throws RecognitionException {
        if (StringUtils.isBlank(imageBase64)) {
            throw new RecognitionException("本地OCR无法读取图片");
        }
        String normalizedBase64 = imageBase64.contains(",") ? imageBase64.substring(imageBase64.indexOf(',') + 1) : imageBase64;
        try {
            byte[] imageBytes = Base64.getDecoder().decode(normalizedBase64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                throw new RecognitionException("本地OCR无法读取图片");
            }
            boolean[][] binaryImage = threshold(image);
            binaryImage = removeIsolatedPixels(binaryImage);
            binaryImage = removeNoiseComponents(binaryImage);
            binaryImage = crop(binaryImage);
            if (binaryImage.length == 0 || binaryImage[0].length == 0) {
                throw new RecognitionException("本地OCR无法读取图片");
            }
            return binaryImage;
        } catch (IllegalArgumentException | IOException e) {
            throw new RecognitionException("本地OCR无法读取图片");
        }
    }

    private boolean[][] threshold(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] grayscale = new int[height][width];
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                int gray = (red * 299 + green * 587 + blue * 114) / 1000;
                grayscale[y][x] = gray;
                histogram[gray]++;
            }
        }
        int threshold = Math.min(220, otsu(histogram, width * height) + 10);
        boolean[][] binary = new boolean[height][width];
        int darkPixels = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                binary[y][x] = grayscale[y][x] <= threshold;
                if (binary[y][x]) {
                    darkPixels++;
                }
            }
        }
        if (darkPixels < Math.max(1, width * height / 200)) {
            int relaxedThreshold = Math.min(240, threshold + 18);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    binary[y][x] = grayscale[y][x] <= relaxedThreshold;
                }
            }
        }
        return binary;
    }

    private int otsu(int[] histogram, int totalPixels) {
        double sum = 0;
        for (int i = 0; i < histogram.length; i++) {
            sum += i * histogram[i];
        }
        double backgroundSum = 0;
        int backgroundWeight = 0;
        double maxVariance = -1;
        int bestThreshold = 127;
        for (int i = 0; i < histogram.length; i++) {
            backgroundWeight += histogram[i];
            if (backgroundWeight == 0) {
                continue;
            }
            int foregroundWeight = totalPixels - backgroundWeight;
            if (foregroundWeight == 0) {
                break;
            }
            backgroundSum += (double) i * histogram[i];
            double backgroundMean = backgroundSum / backgroundWeight;
            double foregroundMean = (sum - backgroundSum) / foregroundWeight;
            double variance = (double) backgroundWeight * foregroundWeight
                    * (backgroundMean - foregroundMean) * (backgroundMean - foregroundMean);
            if (variance > maxVariance) {
                maxVariance = variance;
                bestThreshold = i;
            }
        }
        return bestThreshold;
    }

    private boolean[][] removeIsolatedPixels(boolean[][] source) {
        int height = source.length;
        int width = source[0].length;
        boolean[][] cleaned = copy(source);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!source[y][x]) {
                    continue;
                }
                int neighbors = countDarkNeighbors(source, x, y);
                if (neighbors <= 1) {
                    cleaned[y][x] = false;
                }
            }
        }
        return cleaned;
    }

    private boolean[][] removeNoiseComponents(boolean[][] source) {
        int height = source.length;
        int width = source[0].length;
        boolean[][] cleaned = copy(source);
        boolean[][] visited = new boolean[height][width];
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!source[y][x] || visited[y][x]) {
                    continue;
                }
                List<int[]> component = new ArrayList<>();
                Deque<int[]> queue = new ArrayDeque<>();
                queue.add(new int[]{x, y});
                visited[y][x] = true;
                int minX = x;
                int maxX = x;
                int minY = y;
                int maxY = y;
                while (!queue.isEmpty()) {
                    int[] point = queue.removeFirst();
                    component.add(point);
                    minX = Math.min(minX, point[0]);
                    maxX = Math.max(maxX, point[0]);
                    minY = Math.min(minY, point[1]);
                    maxY = Math.max(maxY, point[1]);
                    for (int[] direction : directions) {
                        int nextX = point[0] + direction[0];
                        int nextY = point[1] + direction[1];
                        if (nextY < 0 || nextY >= height || nextX < 0 || nextX >= width) {
                            continue;
                        }
                        if (!source[nextY][nextX] || visited[nextY][nextX]) {
                            continue;
                        }
                        visited[nextY][nextX] = true;
                        queue.addLast(new int[]{nextX, nextY});
                    }
                }
                int componentWidth = maxX - minX + 1;
                int componentHeight = maxY - minY + 1;
                boolean looksLikeNoise = component.size() <= 2
                        || (componentWidth >= Math.max(12, width / 3) && componentHeight <= Math.max(2, height / 10))
                        || (componentHeight >= Math.max(12, height / 3) && componentWidth <= 1);
                if (looksLikeNoise) {
                    for (int[] point : component) {
                        cleaned[point[1]][point[0]] = false;
                    }
                }
            }
        }
        return cleaned;
    }

    private int countDarkNeighbors(boolean[][] source, int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = x + dx;
                int ny = y + dy;
                if (ny >= 0 && ny < source.length && nx >= 0 && nx < source[0].length && source[ny][nx]) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean[][] crop(boolean[][] source) {
        int minX = source[0].length;
        int minY = source.length;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < source.length; y++) {
            for (int x = 0; x < source[0].length; x++) {
                if (source[y][x]) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < minX || maxY < minY) {
            return new boolean[0][0];
        }
        minX = Math.max(0, minX - 1);
        minY = Math.max(0, minY - 1);
        maxX = Math.min(source[0].length - 1, maxX + 1);
        maxY = Math.min(source.length - 1, maxY + 1);
        boolean[][] cropped = new boolean[maxY - minY + 1][maxX - minX + 1];
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                cropped[y - minY][x - minX] = source[y][x];
            }
        }
        return cropped;
    }

    private List<boolean[][]> splitCharacters(boolean[][] source, int expectedCount) {
        boolean[][] cropped = crop(source);
        if (cropped.length == 0 || cropped[0].length == 0) {
            return List.of();
        }
        List<SegmentBounds> bounds = findBounds(cropped);
        if (expectedCount > 0 && bounds.size() != expectedCount) {
            return splitEvenly(cropped, expectedCount);
        }
        if (bounds.isEmpty()) {
            return expectedCount > 0 ? splitEvenly(cropped, expectedCount) : List.of();
        }
        List<boolean[][]> segments = new ArrayList<>();
        for (SegmentBounds bound : bounds) {
            boolean[][] segment = crop(extract(cropped, bound.startX, bound.endX));
            if (segment.length > 0 && segment[0].length > 0) {
                segments.add(segment);
            }
        }
        return segments;
    }

    private List<SegmentBounds> findBounds(boolean[][] source) {
        List<SegmentBounds> bounds = new ArrayList<>();
        int width = source[0].length;
        int start = -1;
        int blankCount = 0;
        for (int x = 0; x < width; x++) {
            boolean active = hasDarkPixelsInColumn(source, x);
            if (active) {
                if (start < 0) {
                    start = x;
                }
                blankCount = 0;
            } else if (start >= 0) {
                blankCount++;
                if (blankCount > 1) {
                    int end = x - blankCount;
                    if (end >= start) {
                        bounds.add(new SegmentBounds(start, end));
                    }
                    start = -1;
                    blankCount = 0;
                }
            }
        }
        if (start >= 0) {
            bounds.add(new SegmentBounds(start, width - 1 - blankCount));
        }
        return bounds;
    }

    private boolean hasDarkPixelsInColumn(boolean[][] source, int x) {
        int darkCount = 0;
        for (boolean[] row : source) {
            if (row[x]) {
                darkCount++;
            }
        }
        return darkCount >= 1;
    }

    private List<boolean[][]> splitEvenly(boolean[][] source, int expectedCount) {
        List<boolean[][]> segments = new ArrayList<>();
        int width = source[0].length;
        for (int i = 0; i < expectedCount; i++) {
            int start = Math.max(0, (int) Math.floor((double) i * width / expectedCount) - 1);
            int end = Math.min(width - 1, (int) Math.ceil((double) (i + 1) * width / expectedCount));
            boolean[][] segment = crop(extract(source, start, end));
            if (segment.length > 0 && segment[0].length > 0) {
                segments.add(segment);
            }
        }
        return segments;
    }

    private boolean[][] extract(boolean[][] source, int startX, int endX) {
        boolean[][] result = new boolean[source.length][endX - startX + 1];
        for (int y = 0; y < source.length; y++) {
            for (int x = startX; x <= endX; x++) {
                result[y][x - startX] = source[y][x];
            }
        }
        return result;
    }

    private MatchResult matchSegment(boolean[][] source, String allowedCharacters) {
        boolean[][] normalizedSource = normalize(source);
        Map<Character, List<boolean[][]>> templates = DIGITS.equals(allowedCharacters) ? digitTemplates : alphaNumericTemplates;
        char bestCharacter = 0;
        double bestScore = Double.MAX_VALUE;
        double secondBestScore = Double.MAX_VALUE;
        for (int i = 0; i < allowedCharacters.length(); i++) {
            char currentCharacter = allowedCharacters.charAt(i);
            List<boolean[][]> variants = templates.get(currentCharacter);
            if (variants == null) {
                continue;
            }
            for (boolean[][] template : variants) {
                double score = compare(normalizedSource, template);
                if (score < bestScore) {
                    secondBestScore = bestScore;
                    bestScore = score;
                    bestCharacter = currentCharacter;
                } else if (score < secondBestScore) {
                    secondBestScore = score;
                }
            }
        }
        return new MatchResult(bestCharacter, bestScore, secondBestScore);
    }

    private boolean[][] normalize(boolean[][] source) {
        boolean[][] cropped = crop(source);
        if (cropped.length == 0 || cropped[0].length == 0) {
            return new boolean[TARGET_HEIGHT][TARGET_WIDTH];
        }
        BufferedImage target = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, TARGET_WIDTH, TARGET_HEIGHT);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            BufferedImage sourceImage = toImage(cropped);
            double scale = Math.min((TARGET_WIDTH - 4D) / sourceImage.getWidth(), (TARGET_HEIGHT - 4D) / sourceImage.getHeight());
            scale = Math.max(scale, 0.1D);
            int scaledWidth = Math.max(1, (int) Math.round(sourceImage.getWidth() * scale));
            int scaledHeight = Math.max(1, (int) Math.round(sourceImage.getHeight() * scale));
            int drawX = (TARGET_WIDTH - scaledWidth) / 2;
            int drawY = (TARGET_HEIGHT - scaledHeight) / 2;
            graphics.drawImage(sourceImage, drawX, drawY, scaledWidth, scaledHeight, null);
        } finally {
            graphics.dispose();
        }
        return threshold(target);
    }

    private BufferedImage toImage(boolean[][] source) {
        int height = source.length;
        int width = source[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, source[y][x] ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        return image;
    }

    private double compare(boolean[][] left, boolean[][] right) {
        int total = left.length * left[0].length;
        int pixelDiff = 0;
        int[] leftRows = new int[left.length];
        int[] rightRows = new int[right.length];
        int[] leftColumns = new int[left[0].length];
        int[] rightColumns = new int[right[0].length];
        for (int y = 0; y < left.length; y++) {
            for (int x = 0; x < left[0].length; x++) {
                if (left[y][x] != right[y][x]) {
                    pixelDiff++;
                }
                if (left[y][x]) {
                    leftRows[y]++;
                    leftColumns[x]++;
                }
                if (right[y][x]) {
                    rightRows[y]++;
                    rightColumns[x]++;
                }
            }
        }
        int profileDiff = 0;
        for (int i = 0; i < leftRows.length; i++) {
            profileDiff += Math.abs(leftRows[i] - rightRows[i]);
        }
        for (int i = 0; i < leftColumns.length; i++) {
            profileDiff += Math.abs(leftColumns[i] - rightColumns[i]);
        }
        double pixelScore = pixelDiff / (double) total;
        double profileScore = profileDiff / (double) (total * 2);
        return pixelScore * 0.8D + profileScore * 0.2D;
    }

    private Map<Character, List<boolean[][]>> buildTemplates(String allowedCharacters) {
        Map<Character, List<boolean[][]>> templates = new HashMap<>();
        String[] families = {Font.SANS_SERIF, Font.SERIF, Font.MONOSPACED, Font.DIALOG};
        int[] sizes = {24, 26, 28};
        int[] styles = {Font.PLAIN, Font.BOLD};
        for (int i = 0; i < allowedCharacters.length(); i++) {
            char character = allowedCharacters.charAt(i);
            List<boolean[][]> variants = new ArrayList<>();
            for (String family : families) {
                for (int size : sizes) {
                    for (int style : styles) {
                        variants.add(renderTemplate(character, new Font(family, style, size)));
                        if (Character.isLetter(character)) {
                            variants.add(renderTemplate(Character.toLowerCase(character), new Font(family, style, size)));
                        }
                    }
                }
            }
            templates.put(character, variants);
        }
        return templates;
    }

    private boolean[][] renderTemplate(char character, Font font) {
        BufferedImage image = new BufferedImage(RENDER_WIDTH, RENDER_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, RENDER_WIDTH, RENDER_HEIGHT);
            graphics.setColor(Color.BLACK);
            graphics.setFont(font);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            FontMetrics metrics = graphics.getFontMetrics(font);
            String text = String.valueOf(character);
            int x = Math.max(1, (RENDER_WIDTH - metrics.stringWidth(text)) / 2);
            int y = Math.max(metrics.getAscent(), (RENDER_HEIGHT - metrics.getHeight()) / 2 + metrics.getAscent());
            graphics.drawString(text, x, y);
        } finally {
            graphics.dispose();
        }
        return normalize(threshold(image));
    }

    private boolean[][] copy(boolean[][] source) {
        boolean[][] target = new boolean[source.length][source[0].length];
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, target[i], 0, source[i].length);
        }
        return target;
    }

    private static final class SegmentBounds {
        private final int startX;
        private final int endX;

        private SegmentBounds(int startX, int endX) {
            this.startX = startX;
            this.endX = endX;
        }
    }

    private static final class MatchResult {
        private final char character;
        private final double bestScore;
        private final double secondBestScore;

        private MatchResult(char character, double bestScore, double secondBestScore) {
            this.character = character;
            this.bestScore = bestScore;
            this.secondBestScore = secondBestScore;
        }

        private char getCharacter() {
            return character;
        }

        private boolean accepted() {
            return character != 0
                    && bestScore <= MAX_ACCEPTED_SCORE
                    && (secondBestScore == Double.MAX_VALUE || secondBestScore - bestScore >= MIN_SCORE_MARGIN || bestScore <= 0.28D);
        }
    }
}
