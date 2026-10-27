import { z } from '@/lib/zod';

/**
 * Where the app's settings come from, which differs by identity provider.
 *
 * Cognito keeps upstream's arrangement: fetched at runtime so one build serves every AWS
 * environment, with CloudFrontStack.java writing the file at deploy time and the dev server
 * proxying it (vite.config.ts).
 *
 * Keycloak is inlined at build time instead. It exists only for the local docker-compose stack,
 * where the image is built alongside everything else, so a build arg is the simpler lever and
 * the compose file needs nothing mounted — see deployment-compose/docker-compose.yml.
 */
const cognitoConfig = z.object({
  authProvider: z.literal('cognito'),
  cognitoUserPoolId: z.string().min(1),
  cognitoClientId: z.string().min(1),
});

/**
 * `error` as well as `min`: a build arg that was never passed arrives as `undefined`, which trips
 * zod's invalid_type before any length check, so a message attached only to `min` would never be
 * the one shown — and naming the variable is the whole point of having a message here.
 */
function requiredVar(name: string) {
  return z.string({ error: `${name} is required` }).min(1, `${name} is required`);
}

const keycloakConfig = z.object({
  authProvider: z.literal('keycloak'),
  keycloakUrl: requiredVar('VITE_KEYCLOAK_URL'),
  keycloakRealm: requiredVar('VITE_KEYCLOAK_REALM'),
  keycloakClientId: requiredVar('VITE_KEYCLOAK_CLIENT_ID'),
});

export type AppConfig = z.infer<typeof cognitoConfig> | z.infer<typeof keycloakConfig>;

/**
 * Exported for its own tests.
 *
 * `authProvider` is defaulted before parsing so that every config.json written before Keycloak
 * was an option — the AWS environments — keeps loading unchanged.
 */
export function parseCognitoConfig(raw: unknown): AppConfig {
  const withProvider =
    raw !== null && typeof raw === 'object' ? { authProvider: 'cognito', ...(raw as Record<string, unknown>) } : raw;
  return cognitoConfig.parse(withProvider);
}

/** Exported for its own tests; `source` is `import.meta.env` in the app. */
export function parseKeycloakConfig(source: Record<string, unknown>): AppConfig {
  return keycloakConfig.parse({
    authProvider: 'keycloak',
    keycloakUrl: source.VITE_KEYCLOAK_URL,
    keycloakRealm: source.VITE_KEYCLOAK_REALM,
    keycloakClientId: source.VITE_KEYCLOAK_CLIENT_ID,
  });
}

export async function loadConfig(): Promise<AppConfig> {
  // Vite inlines this at build time, so the fetch below is dropped from a Keycloak bundle
  // entirely rather than being skipped at runtime.
  if (import.meta.env.VITE_AUTH_PROVIDER === 'keycloak') {
    return parseKeycloakConfig(import.meta.env);
  }
  const response = await fetch('/config.json');
  return parseCognitoConfig(await response.json());
}
