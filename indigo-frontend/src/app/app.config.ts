import {
  HTTP_INTERCEPTORS,
  provideHttpClient,
  withXsrfConfiguration,
} from '@angular/common/http';
import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';

import { AuthInterceptor, provideAuth } from 'angular-auth-oidc-client';
import { routes } from './app.routes';
import { authConfig } from './auth/auth.config';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(
      withXsrfConfiguration({
        cookieName: 'CSRF-TOKEN',
        headerName: 'X-CSRF-TOKEN',
      }),
    ),
    provideAuth(authConfig),
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ],
};
