package cn.gdeiassistant.integration.library.pojo;

/**
 * 图书馆续借接口返回（防腐层出参），与 OPAC JSON 解耦。
 */
public class LibraryRenewResult {

    public enum Outcome {
        SUCCESS,
        RENEWAL_LIMIT,
        FAILURE
    }

    /**
     * OPAC 用 result=1 同时表示「续借成功」和「达到续借次数上限」，只能靠这条
     * 上游中文文案区分。该知识只留在防腐层；业务代码读取 outcome，不比较原文。
     */
    private static final String RENEWAL_LIMIT_MARKER = "超过最大续借次数";

    private int result;
    private String message;
    private Outcome outcome = Outcome.FAILURE;

    public static LibraryRenewResult fromOpac(int result, String message) {
        LibraryRenewResult parsed = new LibraryRenewResult();
        parsed.setResult(result);
        parsed.setMessage(message);
        if (result == 1) {
            boolean limitReached = message != null && message.contains(RENEWAL_LIMIT_MARKER);
            parsed.setOutcome(limitReached ? Outcome.RENEWAL_LIMIT : Outcome.SUCCESS);
        } else {
            parsed.setOutcome(Outcome.FAILURE);
        }
        return parsed;
    }

    public int getResult() { return result; }
    public void setResult(int result) { this.result = result; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Outcome getOutcome() { return outcome; }
    public void setOutcome(Outcome outcome) { this.outcome = outcome; }
}
