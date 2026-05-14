CREATE UNIQUE INDEX ix_user_account_first_name ON User_Account (lower(first_name));
CREATE UNIQUE INDEX ix_user_account_last_name ON User_Account (lower(last_name));
