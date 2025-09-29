\c payment_api
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
GRANT CREATE ON SCHEMA public TO payment_user;
GRANT USAGE ON SCHEMA public TO payment_user;

\c payment_api_dev
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
GRANT CREATE ON SCHEMA public TO payment_user;
GRANT USAGE ON SCHEMA public TO payment_user;

\c payment_api_test
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
GRANT CREATE ON SCHEMA public TO payment_user;
GRANT USAGE ON SCHEMA public TO payment_user;