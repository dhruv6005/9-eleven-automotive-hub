-- Phase 1 foundation schema. Business workflows and demo data arrive in later phases.
CREATE TABLE users (
    id UUID PRIMARY KEY, email VARCHAR(320) NOT NULL, phone VARCHAR(30), password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL, status VARCHAR(32) NOT NULL, email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255), deleted_at TIMESTAMPTZ, version BIGINT NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX ux_users_email_lower ON users (LOWER(email)) WHERE deleted_at IS NULL;
CREATE INDEX ix_users_phone ON users (phone) WHERE deleted_at IS NULL;

CREATE TABLE customer_profiles (
    user_id UUID PRIMARY KEY REFERENCES users(id), name VARCHAR(160) NOT NULL, address TEXT, notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255), version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_customer_profiles_name ON customer_profiles (LOWER(name));

CREATE TABLE vehicles (
    id UUID PRIMARY KEY, customer_id UUID NOT NULL REFERENCES customer_profiles(user_id), make VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL, variant VARCHAR(100), year INTEGER, reg_number VARCHAR(24) NOT NULL,
    color VARCHAR(50), size_class VARCHAR(32), vin VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255), deleted_at TIMESTAMPTZ, version BIGINT NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX ux_vehicles_reg_number_lower ON vehicles (LOWER(reg_number)) WHERE deleted_at IS NULL;
CREATE INDEX ix_vehicles_reg_number ON vehicles (reg_number);
CREATE INDEX ix_vehicles_customer_id ON vehicles (customer_id) WHERE deleted_at IS NULL;

CREATE TABLE service_categories (
    id UUID PRIMARY KEY, name VARCHAR(120) NOT NULL, description TEXT, display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, created_by VARCHAR(255), deleted_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX ux_service_categories_name_lower ON service_categories (LOWER(name)) WHERE deleted_at IS NULL;

CREATE TABLE services (
    id UUID PRIMARY KEY, category_id UUID NOT NULL REFERENCES service_categories(id), name VARCHAR(160) NOT NULL,
    description TEXT, base_duration_hours NUMERIC(7,2), active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255), deleted_at TIMESTAMPTZ, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_services_category_active ON services (category_id, active) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX ux_services_category_name_lower ON services (category_id, LOWER(name)) WHERE deleted_at IS NULL;

CREATE TABLE service_prices (
    id UUID PRIMARY KEY, service_id UUID NOT NULL REFERENCES services(id), vehicle_size_class VARCHAR(32) NOT NULL,
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0), created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, created_by VARCHAR(255), version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ux_service_prices_service_size UNIQUE (service_id, vehicle_size_class)
);

CREATE TABLE offers (
    id UUID PRIMARY KEY, title VARCHAR(180) NOT NULL, description TEXT, discount_type VARCHAR(16) NOT NULL,
    discount_value NUMERIC(12,2) NOT NULL CHECK (discount_value >= 0), valid_from DATE NOT NULL, valid_to DATE NOT NULL,
    applies_to_all BOOLEAN NOT NULL DEFAULT FALSE, active BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255), deleted_at TIMESTAMPTZ, version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_offers_valid_dates CHECK (valid_to >= valid_from),
    CONSTRAINT ck_offers_discount_type CHECK (discount_type IN ('PERCENT', 'FLAT'))
);
CREATE INDEX ix_offers_active_dates ON offers (active, valid_from, valid_to) WHERE deleted_at IS NULL;
CREATE TABLE offer_services (
    offer_id UUID NOT NULL REFERENCES offers(id) ON DELETE CASCADE, service_id UUID NOT NULL REFERENCES services(id),
    PRIMARY KEY (offer_id, service_id)
);

CREATE TABLE enquiries (
    id UUID PRIMARY KEY, name VARCHAR(160) NOT NULL, phone VARCHAR(30) NOT NULL, email VARCHAR(320),
    vehicle_info VARCHAR(500), service_interest VARCHAR(300), message TEXT, preferred_date DATE,
    status VARCHAR(32) NOT NULL DEFAULT 'NEW', converted_job_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255), version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_enquiries_status_created ON enquiries (status, created_at DESC);
CREATE INDEX ix_enquiries_phone ON enquiries (phone);

CREATE TABLE jobs (
    id UUID PRIMARY KEY, job_number VARCHAR(32) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customer_profiles(user_id), vehicle_id UUID NOT NULL REFERENCES vehicles(id),
    status VARCHAR(40) NOT NULL, intake_notes TEXT, odometer_in INTEGER CHECK (odometer_in IS NULL OR odometer_in >= 0),
    promised_date TIMESTAMPTZ, actual_delivery_date TIMESTAMPTZ,
    total_estimate NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (total_estimate >= 0),
    discount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (discount >= 0),
    tax NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (tax >= 0),
    total_final NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (total_final >= 0),
    progress_percent INTEGER NOT NULL DEFAULT 0 CHECK (progress_percent BETWEEN 0 AND 100), assigned_to UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255), deleted_at TIMESTAMPTZ, version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_jobs_customer_status ON jobs (customer_id, status) WHERE deleted_at IS NULL;
CREATE INDEX ix_jobs_status_promised_date ON jobs (status, promised_date) WHERE deleted_at IS NULL;
CREATE INDEX ix_jobs_created_at ON jobs (created_at DESC);

-- Weights are configured per job so progress can be derived from completed stages.
CREATE TABLE job_stage_weights (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    stage VARCHAR(40) NOT NULL,
    weight NUMERIC(8,3) NOT NULL CHECK (weight > 0),
    stage_order INTEGER NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ux_job_stage_weights_job_stage UNIQUE (job_id, stage),
    CONSTRAINT ux_job_stage_weights_job_order UNIQUE (job_id, stage_order)
);
CREATE SEQUENCE job_number_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE enquiries ADD CONSTRAINT fk_enquiries_converted_job FOREIGN KEY (converted_job_id) REFERENCES jobs(id);

CREATE TABLE job_items (
    id UUID PRIMARY KEY, job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE, service_id UUID REFERENCES services(id),
    description VARCHAR(500) NOT NULL, price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    approved_by_customer BOOLEAN, extra_work BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255), version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_job_items_job_id ON job_items (job_id);

CREATE TABLE job_status_history (
    id UUID PRIMARY KEY, job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE, from_status VARCHAR(40),
    to_status VARCHAR(40) NOT NULL, note TEXT, visible_to_customer BOOLEAN NOT NULL DEFAULT TRUE,
    changed_by UUID REFERENCES users(id), changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_job_status_history_job_changed ON job_status_history (job_id, changed_at DESC);

CREATE TABLE job_media (
    id UUID PRIMARY KEY, job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE, stage VARCHAR(40) NOT NULL,
    url TEXT NOT NULL, caption VARCHAR(500), visible_to_customer BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, uploaded_by UUID REFERENCES users(id)
);
CREATE INDEX ix_job_media_job_stage ON job_media (job_id, stage, uploaded_at DESC);

CREATE TABLE approvals (
    id UUID PRIMARY KEY, job_id UUID NOT NULL REFERENCES jobs(id) ON DELETE CASCADE,
    item_id UUID NOT NULL REFERENCES job_items(id), status VARCHAR(24) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, responded_at TIMESTAMPTZ, customer_note TEXT,
    CONSTRAINT ck_approval_status CHECK (status IN ('PENDING', 'APPROVED', 'DECLINED'))
);
CREATE INDEX ix_approvals_job_status ON approvals (job_id, status);

CREATE TABLE invoices (
    id UUID PRIMARY KEY, job_id UUID NOT NULL UNIQUE REFERENCES jobs(id), invoice_number VARCHAR(48) NOT NULL UNIQUE,
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, due_at TIMESTAMPTZ,
    subtotal NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0), discount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (discount >= 0),
    tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0), total NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    status VARCHAR(24) NOT NULL DEFAULT 'DRAFT', gstin VARCHAR(15), place_of_supply VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255), version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE invoice_lines (
    id UUID PRIMARY KEY, invoice_id UUID NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL, quantity NUMERIC(10,2) NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0), tax_rate NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (tax_rate >= 0),
    line_total NUMERIC(12,2) NOT NULL CHECK (line_total >= 0)
);
CREATE INDEX ix_invoice_lines_invoice_id ON invoice_lines (invoice_id);

CREATE TABLE payments (
    id UUID PRIMARY KEY, invoice_id UUID NOT NULL REFERENCES invoices(id), amount NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    method VARCHAR(20) NOT NULL, status VARCHAR(24) NOT NULL DEFAULT 'RECORDED', reference VARCHAR(120),
    idempotency_key VARCHAR(120) UNIQUE, paid_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recorded_by UUID REFERENCES users(id), note VARCHAR(500)
);
CREATE INDEX ix_payments_invoice_id ON payments (invoice_id, paid_at DESC);

CREATE TABLE notifications (
    id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, type VARCHAR(40) NOT NULL,
    title VARCHAR(180) NOT NULL, body TEXT NOT NULL, is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, read_at TIMESTAMPTZ
);
CREATE INDEX ix_notifications_user_unread ON notifications (user_id, is_read, created_at DESC);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(128) NOT NULL UNIQUE, expires_at TIMESTAMPTZ NOT NULL, revoked_at TIMESTAMPTZ,
    replaced_by_token_id UUID REFERENCES refresh_tokens(id), created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_agent VARCHAR(500), ip_address INET
);
CREATE INDEX ix_refresh_tokens_user_expiry ON refresh_tokens (user_id, expires_at);
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(128) NOT NULL UNIQUE, expires_at TIMESTAMPTZ NOT NULL, consumed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX ix_password_reset_tokens_user_expiry ON password_reset_tokens (user_id, expires_at);

CREATE TABLE gallery_items (
    id UUID PRIMARY KEY, title VARCHAR(180) NOT NULL, description TEXT, before_image_url TEXT, after_image_url TEXT,
    service_id UUID REFERENCES services(id), display_order INTEGER NOT NULL DEFAULT 0, published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255), version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_gallery_items_published_order ON gallery_items (published, display_order);
CREATE TABLE testimonials (
    id UUID PRIMARY KEY, customer_name VARCHAR(160) NOT NULL, content TEXT NOT NULL,
    rating SMALLINT CHECK (rating IS NULL OR rating BETWEEN 1 AND 5), published BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255), version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_testimonials_published ON testimonials (published, created_at DESC);

CREATE TABLE audit_log (
    id UUID PRIMARY KEY, user_id UUID REFERENCES users(id), action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(120) NOT NULL, entity_id UUID, details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP, correlation_id VARCHAR(80)
);
CREATE INDEX ix_audit_log_entity ON audit_log (entity_type, entity_id, created_at DESC);
CREATE INDEX ix_audit_log_user_created ON audit_log (user_id, created_at DESC);
