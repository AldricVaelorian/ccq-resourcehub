ALTER TABLE bookings
    ADD COLUMN rejected_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN rejection_reason VARCHAR(1000);
