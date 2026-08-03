import { environment } from '@/environments/environment';
import { provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import { ApplicationConfig } from '@angular/core';
import {
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  includeBearerTokenInterceptor,
  provideKeycloak,
} from 'keycloak-angular';
import { commonProviders, sharedHttpFeatures } from './app.config.shared';
import { API_BEARER_TOKEN_CONDITION, buildKeycloakOptions } from './keycloak.config';

/**
 * Keycloak bootstrap configuration, used by the `local` build configuration in angular.json
 * (`npm run start-local`, and the frontend container in deployment-compose).
 * Only auth-specific providers belong here — everything else goes in app.config.shared.ts.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    ...commonProviders,
    provideKeycloak(buildKeycloakOptions(environment.keycloak, window.location.origin)),
    provideHttpClient(
      ...sharedHttpFeatures(),
      withInterceptors([includeBearerTokenInterceptor]),
      withInterceptorsFromDi(),
    ),
    // Required: includeBearerTokenInterceptor injects this token and it has no fallback factory,
    // so omitting it makes every HTTP request fail with NullInjectorError.
    { provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG, useValue: [API_BEARER_TOKEN_CONDITION] },
  ],
};
