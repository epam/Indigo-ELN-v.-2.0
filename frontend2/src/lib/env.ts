import {z} from '@/lib/zod';

const envSchema = z.object({
  VITE_COGNITO_USER_POOL_ID: z.string().min(1, 'VITE_COGNITO_USER_POOL_ID is required'),
  VITE_COGNITO_CLIENT_ID: z.string().min(1, 'VITE_COGNITO_CLIENT_ID is required'),
});

const parsed = envSchema.safeParse(import.meta.env);

if (!parsed.success) {
  throw new Error(
    `Invalid environment configuration. Copy .env.example to .env.local.\n${z.prettifyError(parsed.error)}`,
  );
}

export const env = parsed.data;
