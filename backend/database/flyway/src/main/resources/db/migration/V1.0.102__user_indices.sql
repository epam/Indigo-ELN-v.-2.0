DROP INDEX ix_user_account_display_name;
CREATE INDEX ix_user_account_display_name ON User_Account (lower(display_name));
CREATE INDEX ix_user_account_first_name ON User_Account (lower(first_name));
CREATE INDEX ix_user_account_last_name ON User_Account (lower(last_name));
