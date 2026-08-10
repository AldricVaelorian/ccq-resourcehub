-- Create block_times table
CREATE TABLE IF NOT EXISTS block_times (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_blocked BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add index on resource_id for faster queries
CREATE INDEX IF NOT EXISTS idx_block_times_resource_id ON block_times(resource_id);

-- Add index on start_date and end_date for overlap queries
CREATE INDEX IF NOT EXISTS idx_block_times_date_range ON block_times(start_date, end_date);

-- Add comment for documentation
COMMENT ON TABLE block_times IS 'Block time periods for resources (maintenance, holidays, etc.)';
COMMENT ON COLUMN block_times.id IS 'Unique identifier for the block time';
COMMENT ON COLUMN block_times.resource_id IS 'Reference to the resource';
COMMENT ON COLUMN block_times.title IS 'Title of the block time period';
COMMENT ON COLUMN block_times.description IS 'Description of the block time period';
COMMENT ON COLUMN block_times.start_date IS 'Start date of the block time period';
COMMENT ON COLUMN block_times.end_date IS 'End date of the block time period';
COMMENT ON COLUMN block_times.is_blocked IS 'Indicates if the resource is blocked during this period';
COMMENT ON COLUMN block_times.created_at IS 'Timestamp when the block time was created';
COMMENT ON COLUMN block_times.updated_at IS 'Timestamp when the block time was last updated';