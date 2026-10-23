# Target ER diagram

The schema is introduced in the Phase 1 Flyway migration so later phases can implement domain workflows against stable table names. The job state machine, progress weighting rules and all business CRUD behaviors are implemented in their assigned phases.

```mermaid
erDiagram
    USERS ||--o| CUSTOMER_PROFILES : has
    USERS ||--o{ REFRESH_TOKENS : owns
    USERS ||--o{ PASSWORD_RESET_TOKENS : resets
    USERS ||--o{ NOTIFICATIONS : receives
    USERS ||--o{ AUDIT_LOG : performs
    CUSTOMER_PROFILES ||--o{ VEHICLES : owns
    CUSTOMER_PROFILES ||--o{ JOBS : requests
    SERVICE_CATEGORIES ||--o{ SERVICES : groups
    SERVICES ||--o{ SERVICE_PRICES : priced_for
    SERVICES ||--o{ JOB_ITEMS : selected_for
    SERVICES ||--o{ OFFER_SERVICES : eligible_for
    SERVICES ||--o{ GALLERY_ITEMS : represents
    VEHICLES ||--o{ JOBS : serviced_by
    JOBS ||--o{ JOB_ITEMS : contains
    JOBS ||--o{ JOB_STATUS_HISTORY : records
    JOBS ||--o{ JOB_STAGE_WEIGHTS : configures
    JOBS ||--o{ JOB_MEDIA : documents
    JOBS ||--o{ APPROVALS : requests
    JOB_ITEMS ||--o{ APPROVALS : requires
    OFFERS ||--o{ OFFER_SERVICES : includes
    JOBS ||--o| INVOICES : billed_by
    INVOICES ||--o{ INVOICE_LINES : contains
    INVOICES ||--o{ PAYMENTS : paid_by
    ENQUIRIES o|--o| JOBS : converts_to

    USERS {
        uuid id PK
        string email UK
        string phone
        string password_hash
        string role
        string status
        boolean email_verified
    }
    CUSTOMER_PROFILES {
        uuid user_id PK,FK
        string name
        string address
        text notes
    }
    VEHICLES {
        uuid id PK
        uuid customer_id FK
        string make
        string model
        string reg_number UK
        string size_class
    }
    SERVICE_CATEGORIES {
        uuid id PK
        string name
        boolean active
    }
    SERVICES {
        uuid id PK
        uuid category_id FK
        string name
        decimal base_duration_hours
        boolean active
    }
    SERVICE_PRICES {
        uuid id PK
        uuid service_id FK
        string vehicle_size_class
        decimal price
    }
    OFFERS {
        uuid id PK
        string title
        string discount_type
        decimal discount_value
        date valid_from
        date valid_to
        boolean active
    }
    OFFER_SERVICES {
        uuid offer_id PK,FK
        uuid service_id PK,FK
    }
    ENQUIRIES {
        uuid id PK
        string name
        string phone
        string status
        uuid converted_job_id FK
    }
    JOBS {
        uuid id PK
        string job_number UK
        uuid customer_id FK
        uuid vehicle_id FK
        string status
        decimal total_estimate
        decimal total_final
        int progress_percent
    }
    JOB_ITEMS {
        uuid id PK
        uuid job_id FK
        uuid service_id FK
        decimal price
        boolean extra_work
    }
    JOB_STAGE_WEIGHTS {
        uuid id PK
        uuid job_id FK
        string stage
        decimal weight
        int stage_order
        datetime completed_at
    }
    JOB_STATUS_HISTORY {
        uuid id PK
        uuid job_id FK
        string from_status
        string to_status
        boolean visible_to_customer
        datetime changed_at
    }
    JOB_MEDIA {
        uuid id PK
        uuid job_id FK
        string stage
        string url
        boolean visible_to_customer
    }
    APPROVALS {
        uuid id PK
        uuid job_id FK
        uuid item_id FK
        string status
        datetime requested_at
        datetime responded_at
    }
    INVOICES {
        uuid id PK
        uuid job_id FK
        string invoice_number UK
        decimal subtotal
        decimal tax_amount
        decimal total
    }
    INVOICE_LINES {
        uuid id PK
        uuid invoice_id FK
        string description
        decimal quantity
        decimal unit_price
    }
    PAYMENTS {
        uuid id PK
        uuid invoice_id FK
        decimal amount
        string method
        string status
    }
    NOTIFICATIONS {
        uuid id PK
        uuid user_id FK
        string type
        string title
        boolean is_read
    }
    REFRESH_TOKENS {
        uuid id PK
        uuid user_id FK
        string token_hash
        datetime expires_at
        datetime revoked_at
    }
    PASSWORD_RESET_TOKENS {
        uuid id PK
        uuid user_id FK
        string token_hash
        datetime expires_at
        datetime consumed_at
    }
    GALLERY_ITEMS {
        uuid id PK
        string title
        string before_image_url
        string after_image_url
        boolean published
    }
    TESTIMONIALS {
        uuid id PK
        string customer_name
        text content
        int rating
        boolean published
    }
    AUDIT_LOG {
        uuid id PK
        uuid user_id FK
        string action
        string entity_type
        uuid entity_id
        datetime created_at
    }
```

## Required indexes included

- `jobs(customer_id, status)` — partial index for non-deleted jobs.
- `vehicles(reg_number)` plus a case-insensitive unique index for active registration numbers.
- `job_status_history(job_id, changed_at DESC)`.
- `job_stage_weights(job_id, stage_order)` for ordered, per-job progress weights; progress is derived from completed stages.
- `job_number_seq` provides a sequential counter for human-friendly job numbers.
- Supporting indexes cover job queues, service catalog lookups, customer notifications, enquiry triage, invoice lines and audit history.
