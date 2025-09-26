-- Enable UUID extension for PostgreSQL

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant permissions
GRANT CREATE ON SCHEMA public TO payment_user;
GRANT USAGE ON SCHEMA public TO payment_user;