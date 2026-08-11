CREATE TABLE availability_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_availability_rules_time_window CHECK (end_time > start_time)
);

CREATE INDEX idx_availability_rules_day_of_week ON availability_rules(day_of_week);
CREATE INDEX idx_availability_rules_active ON availability_rules(active);
