package cn.gdeiassistant.core.capability.ai;

import java.util.List;
import java.util.Objects;

/** Provider-neutral text/image conversation. No credentials or routing policy in request data. */
public record AiRequest(List<Message> messages, int maxOutputTokens) {
    public enum Role { SYSTEM, USER, ASSISTANT }
    public record Image(String mediaType, String base64) {
        public Image {
            if (!List.of("image/png", "image/jpeg", "image/webp", "image/gif").contains(mediaType)
                    || base64 == null || base64.isBlank()) throw new IllegalArgumentException("Invalid AI image input");
        }
        @Override public String toString() { return "Image[mediaType=" + mediaType + "]"; }
    }
    public record Message(Role role, String text, List<Image> images) {
        public Message {
            Objects.requireNonNull(role);
            text = text == null ? "" : text;
            images = List.copyOf(images);
            if (text.isBlank() && images.isEmpty()) throw new IllegalArgumentException("Empty AI message");
            if (role != Role.USER && !images.isEmpty()) throw new IllegalArgumentException("Images require a user message");
        }
        public static Message text(Role role, String text) { return new Message(role, text, List.of()); }
        @Override public String toString() { return "Message[role=" + role + ", images=" + images.size() + "]"; }
    }
    public AiRequest {
        messages = List.copyOf(messages);
        if (messages.isEmpty() || messages.stream().noneMatch(message -> message.role() == Role.USER)) {
            throw new IllegalArgumentException("AI request requires a user message");
        }
        if (maxOutputTokens < 1 || maxOutputTokens > 16384) throw new IllegalArgumentException("Invalid AI output token limit");
    }
    public static AiRequest text(String prompt) {
        return new AiRequest(List.of(Message.text(Role.USER, prompt)), 1024);
    }
    public static AiRequest image(String prompt, String mediaType, String base64) {
        return new AiRequest(List.of(new Message(Role.USER, prompt, List.of(new Image(mediaType, base64)))), 256);
    }
    @Override public String toString() { return "AiRequest[messages=" + messages.size() + ", maxOutputTokens=" + maxOutputTokens + "]"; }
}
