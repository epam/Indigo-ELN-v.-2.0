import { z } from '@/lib/zod';

const configSchema = z.object({
  cognitoUserPoolId: z.string().min(1),
  cognitoClientId: z.string().min(1),
});

/**
 * Per-environment settings, fetched at runtime so that one build serves every environment.
 * CloudFrontStack.java writes the file at deploy time; the dev server proxies it (vite.config.ts).
 */
export async function loadConfig() {
  const response = await fetch('/config.json');
  return configSchema.parse(await response.json());
}
