-- Add UNIQUE constraint on idempotency_key_hash to prevent duplicate charges.
-- Requires the 2026-04-28 migration to have been applied first.

USE gdeiassistant_log;

-- Remove the non-unique index first to replace it with a UNIQUE constraint
ALTER TABLE `charge_order` DROP INDEX `idx_charge_order_idempotency_hash`;

-- Add UNIQUE constraint as the database-level fallback for idempotency
ALTER TABLE `charge_order` ADD UNIQUE INDEX `uq_charge_order_idempotency_hash` (`idempotency_key_hash`);
