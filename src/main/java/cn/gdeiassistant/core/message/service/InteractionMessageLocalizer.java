package cn.gdeiassistant.core.message.service;

import cn.gdeiassistant.common.i18n.ApiLanguageResolver;
import cn.gdeiassistant.common.i18n.MessageCatalog;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Localizes user-generated interaction notifications.
 *
 * <p>Notification titles and bodies are stored as Simplified Chinese text, so their templates are
 * matched against the catalog instead of carrying a key. The catalog itself lives in
 * {@code common.i18n}; only this core-level translator knows about {@link InteractionMessageVO}.
 */
public final class InteractionMessageLocalizer {

    private static final Pattern COMMENT_WITH_DETAIL = Pattern.compile("^(.+?) ?评论了你的.+?[：:](.+)$", Pattern.DOTALL);
    private static final Pattern SIMPLE_COMMENT = Pattern.compile("^(.+?) ?评论了你的(?:树洞|表白|作品)$");
    private static final Pattern DELIVERY_ACCEPTED = Pattern.compile("^(.+?) 接取了你发布的 (.+) 订单$");
    private static final Pattern DELIVERY_FINISHED = Pattern.compile("^(.+?) 已确认你接取的 (.+) 订单完成$");
    private static final Pattern DATING_ACCEPTED = Pattern.compile("^(.+?) 通过了你的请求$");
    private static final Pattern DATING_REJECTED = Pattern.compile("^(.+?) 拒绝了你的请求$");
    private static final Pattern DATING_PICK_WITH_DETAIL = Pattern.compile("^(.+?) 向你发起了撩一下[：:](.+)$");
    private static final Pattern DATING_PICK = Pattern.compile("^(.+?) 向你发起了撩一下$");
    private static final Pattern GUESS_CORRECT = Pattern.compile("^(.+?) 猜中了你的表白对象$");
    private static final Pattern GUESS_JOINED = Pattern.compile("^(.+?) 参与了你的猜名字$");
    private static final Pattern SIMPLE_LIKE = Pattern.compile("^(.+?) 点赞了你的.+$");

    private InteractionMessageLocalizer() {
    }

    public static InteractionMessageVO localize(InteractionMessageVO source, String language) {
        if (source == null) {
            return null;
        }
        String normalizedLanguage = ApiLanguageResolver.normalizeLanguage(language);
        if ("zh-CN".equals(normalizedLanguage)) {
            return source;
        }
        InteractionMessageVO localized = new InteractionMessageVO();
        localized.setId(source.getId());
        localized.setModule(source.getModule());
        localized.setType(source.getType());
        localized.setCreatedAt(source.getCreatedAt());
        localized.setIsRead(source.getIsRead());
        localized.setTargetType(source.getTargetType());
        localized.setTargetId(source.getTargetId());
        localized.setTargetSubId(source.getTargetSubId());
        localized.setTitle(localizeTitle(source.getModule(), source.getType(), source.getTitle(), normalizedLanguage));
        localized.setContent(localizeContent(source.getModule(), source.getType(), source.getContent(), normalizedLanguage));
        return localized;
    }

    private static String localizeTitle(String module, String type, String fallbackTitle, String language) {
        String key = switch ((safe(module) + ":" + safe(type))) {
            case "secret:comment" -> "interaction.secret.comment.title";
            case "secret:like" -> "interaction.secret.like.title";
            case "express:comment" -> "interaction.express.comment.title";
            case "express:like" -> "interaction.express.like.title";
            case "express:guess" -> "interaction.express.guess.title";
            case "topic:like" -> "interaction.topic.like.title";
            case "photograph:comment" -> "interaction.photograph.comment.title";
            case "photograph:like" -> "interaction.photograph.like.title";
            case "dating:pick_received" -> "interaction.dating.received.title";
            case "dating:pick_accepted" -> "interaction.dating.accepted.title";
            case "dating:pick_rejected" -> "interaction.dating.rejected.title";
            case "delivery:order_accepted" -> "interaction.delivery.accepted.title";
            case "delivery:order_finished" -> "interaction.delivery.finished.title";
            default -> null;
        };
        if (key == null) {
            return MessageCatalog.localizeMessage(fallbackTitle, language);
        }
        return MessageCatalog.translate(key, language, MessageCatalog.localizeMessage(fallbackTitle, language));
    }

    private static String localizeContent(String module, String type, String content, String language) {
        if (content == null || content.isBlank()) {
            return content;
        }
        String key = safe(module) + ":" + safe(type);
        return switch (key) {
            case "secret:comment" -> formatComment(content, language, "interaction.secret.comment.content", "interaction.secret.comment.contentWithDetail");
            case "secret:like" -> formatActorOnly(content, SIMPLE_LIKE, language, "interaction.secret.like.content");
            case "express:comment" -> formatComment(content, language, "interaction.express.comment.content", "interaction.express.comment.contentWithDetail");
            case "express:like" -> formatActorOnly(content, SIMPLE_LIKE, language, "interaction.express.like.content");
            case "express:guess" -> formatGuess(content, language);
            case "topic:like" -> formatActorOnly(content, SIMPLE_LIKE, language, "interaction.topic.like.content");
            case "photograph:comment" -> formatComment(content, language, "interaction.photograph.comment.content", "interaction.photograph.comment.contentWithDetail");
            case "photograph:like" -> formatActorOnly(content, SIMPLE_LIKE, language, "interaction.photograph.like.content");
            case "dating:pick_received" -> formatDatingPick(content, language);
            case "dating:pick_accepted" -> formatNickname(content, DATING_ACCEPTED, language, "interaction.dating.accepted.content");
            case "dating:pick_rejected" -> formatNickname(content, DATING_REJECTED, language, "interaction.dating.rejected.content");
            case "delivery:order_accepted" -> formatDelivery(content, DELIVERY_ACCEPTED, language, "interaction.delivery.accepted.content", "interaction.delivery.accepted.contentWithCompany");
            case "delivery:order_finished" -> formatDelivery(content, DELIVERY_FINISHED, language, "interaction.delivery.finished.content", "interaction.delivery.finished.contentWithCompany");
            default -> MessageCatalog.localizeMessage(content, language);
        };
    }

    private static String formatComment(String content, String language, String baseKey, String detailKey) {
        Matcher matcher = COMMENT_WITH_DETAIL.matcher(content);
        if (matcher.matches()) {
            return MessageCatalog.format(detailKey, language, matcher.group(1), matcher.group(2));
        }
        return formatActorOnly(content, SIMPLE_COMMENT, language, baseKey);
    }

    private static String formatActorOnly(String content, Pattern pattern, String language, String key) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.matches()) {
            return MessageCatalog.format(key, language, matcher.group(1));
        }
        return MessageCatalog.localizeMessage(content, language);
    }

    private static String formatDatingPick(String content, String language) {
        Matcher detailMatcher = DATING_PICK_WITH_DETAIL.matcher(content);
        if (detailMatcher.matches()) {
            return MessageCatalog.format("interaction.dating.received.contentWithDetail", language,
                    detailMatcher.group(1), detailMatcher.group(2).trim());
        }
        Matcher matcher = DATING_PICK.matcher(content);
        if (matcher.matches()) {
            return MessageCatalog.format("interaction.dating.received.content", language, matcher.group(1));
        }
        return MessageCatalog.localizeMessage(content, language);
    }

    private static String formatNickname(String content, Pattern pattern, String language, String key) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.matches()) {
            return MessageCatalog.format(key, language, matcher.group(1));
        }
        return MessageCatalog.localizeMessage(content, language);
    }

    private static String formatDelivery(String content, Pattern pattern, String language, String actorKey, String actorCompanyKey) {
        Matcher matcher = pattern.matcher(content);
        if (!matcher.matches()) {
            return MessageCatalog.localizeMessage(content, language);
        }
        String actor = matcher.group(1);
        String company = matcher.groupCount() >= 2 ? matcher.group(2) : null;
        if (company == null || company.isBlank()) {
            return MessageCatalog.format(actorKey, language, actor);
        }
        return MessageCatalog.format(actorCompanyKey, language, actor, company.trim());
    }

    private static String formatGuess(String content, String language) {
        Matcher correctMatcher = GUESS_CORRECT.matcher(content);
        if (correctMatcher.matches()) {
            return MessageCatalog.format("interaction.express.guess.correct", language, correctMatcher.group(1));
        }
        Matcher joinedMatcher = GUESS_JOINED.matcher(content);
        if (joinedMatcher.matches()) {
            return MessageCatalog.format("interaction.express.guess.joined", language, joinedMatcher.group(1));
        }
        return MessageCatalog.localizeMessage(content, language);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
