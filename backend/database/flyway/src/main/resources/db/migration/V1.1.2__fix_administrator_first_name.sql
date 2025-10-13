UPDATE User_Account SET first_name = 'Administrator' WHERE username = 'admin';

ALTER TABLE User_Account ALTER COLUMN first_name SET NOT NULL;
ALTER TABLE User_Account ALTER COLUMN last_name SET NOT NULL;
