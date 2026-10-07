package cn.gdeiassistant.core.i18n;

import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class BackendLocaleCoverageTest {

    @Test
    @SuppressWarnings("unchecked")
    void allSystemMessagesHaveSixNonEmptyTranslationsAndMatchingPlaceholders() {
        Map<String, Map<String, String>> messages = (Map<String, Map<String, String>>)
                ReflectionTestUtils.getField(BackendTextLocalizer.class, "MESSAGE_TRANSLATIONS");
        assertNotNull(messages);
        Set<String> locales = Set.of("zh-CN", "zh-HK", "zh-TW", "en", "ja", "ko");
        messages.forEach((key, translations) -> {
            assertEquals(locales, translations.keySet(), key);
            Set<String> expected = placeholders(translations.get("zh-CN"));
            translations.forEach((locale, text) -> {
                assertFalse(text.isBlank(), key + ":" + locale);
                assertEquals(expected, placeholders(text), key + ":" + locale);
            });
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "ja", "ko"})
    void newlyCoveredValidationMessagesDoNotFallBackToChinese(String locale) {
        for (String message : new String[]{"未检测到有效令牌", "缺少 sn 或 code", "请提供图书馆密码",
                "准考证号不能为空", "准考证号必须为15位", "准考证号必须为15位数字", "请求体不能为空",
                "fileName 不能为空", "contentType 不合法", "不支持的文件类型: ", "文件扩展名与 contentType 不匹配"}) {
            assertNotEquals(message, BackendTextLocalizer.localizeMessage(message, locale), locale + ":" + message);
        }
    }

    @Test
    void cantoneseNotificationPreservesActorAndUserCommentVerbatim() {
        InteractionMessageVO message = new InteractionMessageVO();
        message.setModule("secret");
        message.setType("comment");
        message.setContent("林同学 评论了你的树洞：一起加油");
        InteractionMessageVO hk = BackendTextLocalizer.localizeInteractionMessage(message, "ZH_hant_HK");
        assertEquals("林同学 留言回應咗你嘅樹洞：一起加油", hk.getContent());
        assertEquals("林同学 评论了你的树洞：一起加油", message.getContent());
        assertEquals("林同学 評論了你的樹洞：一起加油",
                BackendTextLocalizer.localizeInteractionMessage(message, "zh-TW").getContent());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "secret|树洞|en|林同学 commented on your confession",
            "secret|树洞|ja|林同学 さんがあなたのツリーホールにコメントしました",
            "secret|树洞|ko|林同学님이 당신의 트리홀에 댓글을 남겼습니다",
            "express|表白|en|林同学 commented on your confession wall post",
            "express|表白|ja|林同学 さんがあなたの告白にコメントしました",
            "express|表白|ko|林同学님이 당신의 고백 글에 댓글을 남겼습니다",
            "photograph|作品|en|林同学 commented on your post",
            "photograph|作品|ja|林同学 さんがあなたの作品にコメントしました",
            "photograph|作品|ko|林同学님이 당신의 작품에 댓글을 남겼습니다",
            "secret|树洞|zh-HK|林同学 留言回應咗你嘅樹洞",
            "secret|树洞|zh-TW|林同学 評論了你的樹洞"
    })
    void translatesCommentsWithoutDetails(String module, String target, String locale, String expected) {
        InteractionMessageVO message = new InteractionMessageVO();
        message.setModule(module);
        message.setType("comment");
        String original = "林同学 评论了你的" + target;
        message.setContent(original);
        assertEquals(expected, BackendTextLocalizer.localizeInteractionMessage(message, locale).getContent());
        assertEquals(original, message.getContent());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "secret|林同学 评论了你的树洞|林同学 commented on your confession",
            "express|有人评论了你的表白|有人 commented on your confession wall post",
            "photograph|林同学 评论了你的作品|林同学 commented on your post"
    })
    void translatesMultilineCommentsAndPreservesUserText(String module, String prefix, String expectedPrefix) {
        String detail = "  第一行：内容\n第二行 {0} '原文'  ";
        InteractionMessageVO message = new InteractionMessageVO();
        message.setModule(module);
        message.setType("comment");
        String original = prefix + "：" + detail;
        message.setContent(original);
        assertEquals(expectedPrefix + ": " + detail,
                BackendTextLocalizer.localizeInteractionMessage(message, "en-US").getContent());
        assertEquals(original, message.getContent());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "en|Invalid beforeSeq", "ja|beforeSeq が無効です", "ko|beforeSeq 값이 올바르지 않습니다",
            "zh-HK|beforeSeq 唔啱", "zh-TW|beforeSeq 無效", "zh-CN|beforeSeq 无效"
    })
    void translatesDynamicValidationWithoutTranslatingTheProtocolField(String locale, String expected) {
        assertEquals(expected, BackendTextLocalizer.localizeMessage("beforeSeq 无效", locale));
    }

    private static Set<String> placeholders(String text) {
        Set<String> result = new TreeSet<>();
        Pattern.compile("\\{\\d+\\}").matcher(text).results().forEach(match -> result.add(match.group()));
        return result;
    }
}
