import { IKeycloakConfig } from '@core/types/environment.i';
import {
  AutoRefreshTokenService,
  createInterceptorCondition,
  IncludeBearerTokenCondition,
  ProvideKeycloakOptions,
  UserActivityService,
  withAutoRefreshToken,
} from 'keycloak-angular';

/**
 * Keycloak wiring, kept out of app.config.local.ts so it stays testable: this file is not part
 * of the `local` fileReplacements set, so specs compile it under the default tsconfig.spec.json.
 */

/** Log the user out after this much browser inactivity. */
export const SESSION_TIMEOUT_MS = 30 * 60 * 1000;

/**
 * Attaches the bearer token to the ELN backend endpoints only — the ones nginx routes to the
 * services. Anything else (assets, the dev-server bundles) must not receive the token.
 *
 * Note this condition fails *open*: a request that does not match simply goes out without an
 * Authorization header and comes back 401, so keep keycloak.config.spec.ts in sync with it.
 */
export const API_BEARER_TOKEN_CONDITION = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^(https?:\/\/[^/]+)?\/(api|internalapi)\//i,
});

export const buildKeycloakOptions = (config: IKeycloakConfig, origin: string): ProvideKeycloakOptions => ({
  config: {
    url: config.url,
    realm: config.realm,
    clientId: config.clientId,
  },
  initOptions: {
    onLoad: 'login-required',
    redirectUri: `${origin}/`,
  },
  features: [
    withAutoRefreshToken({
      onInactivityTimeout: 'logout',
      sessionTimeout: SESSION_TIMEOUT_MS,
    }),
  ],
  providers: [AutoRefreshTokenService, UserActivityService],
});
