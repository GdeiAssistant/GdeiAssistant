package cn.gdeiassistant.common.pojo.dto;

/** Sensitive credentials belong in a request body, never in a query string. */
public record PasswordRequest(String password) {}
