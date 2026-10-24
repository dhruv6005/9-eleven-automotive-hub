ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
DROP INDEX ux_users_email_lower;
CREATE UNIQUE INDEX ux_users_email_lower ON users (LOWER(email)) WHERE deleted_at IS NULL AND email IS NOT NULL;
CREATE INDEX ix_vehicles_active_registration ON vehicles (customer_id, reg_number) WHERE deleted_at IS NULL;
