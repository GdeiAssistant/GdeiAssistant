package cn.gdeiassistant.common.account;

import java.util.List;

/**
 * What one domain owes the account-closure flow. Each domain decides how its own data
 * blocks, is erased, or is cleaned up; the closure flow only orchestrates and never
 * learns another domain's tables or state machines.
 *
 * <p>{@link #blockers} runs before the user asks for closure, {@link #purge} runs inside
 * the application transaction after the user row is locked, and {@link #cleanupAfterCommit}
 * runs after commit through the retry queue, so it must be repeatable.
 */
public interface AccountLifecycleContributor {

    /**
     * Unfinished content in this domain that should block closure. Nothing blocks by default.
     */
    default List<AccountCloseBlocker> blockers(String username) {
        return List.of();
    }

    /**
     * Erase or anonymize this domain's data. Domains without user data keep the default.
     */
    default void purge(AccountClosure closure) {
    }

    /**
     * Cross-store cleanup (caches, object storage, live connections). Must be idempotent.
     */
    default void cleanupAfterCommit(AccountClosure closure) {
    }

    /**
     * Interpret this domain's own fields in the data export (status codes, dictionaries,
     * location codes). The orchestrator owns the field whitelist; the domain owns the meaning.
     */
    default void decorateExport(AccountDataExport export) {
    }
}
