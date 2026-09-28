DROP INDEX ix_user_account_display_name;
DROP INDEX ix_user_account_first_name;
DROP INDEX ix_user_account_last_name;

CREATE INDEX ix_user_account_display_name ON User_Account USING GIN (display_name gin_trgm_ops);
CREATE INDEX ix_user_account_first_name ON User_Account USING GIN (first_name gin_trgm_ops);
CREATE INDEX ix_user_account_last_name ON User_Account USING GIN (last_name gin_trgm_ops);
CREATE INDEX ix_user_account_username ON User_Account USING GIN (username gin_trgm_ops);
