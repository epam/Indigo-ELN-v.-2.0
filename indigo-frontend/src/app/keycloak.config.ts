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

export const API_BEARER_TOKEN_CONDITION = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^\/(api|internalapi)\//i,
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
