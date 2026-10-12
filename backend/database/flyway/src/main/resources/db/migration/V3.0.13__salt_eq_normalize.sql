UPDATE Compound SET salt_eq = trim_scale(salt_eq) WHERE salt_eq IS NOT NULL;
