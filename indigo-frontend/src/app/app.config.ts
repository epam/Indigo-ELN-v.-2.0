import { JwtInterceptor } from '@/core/interceptors/jwt.interceptor';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { ApplicationConfig } from '@angular/core';
import { commonProviders, sharedHttpFeatures } from './app.config.shared';

/**
 * Cognito bootstrap configuration (default / staging / production builds).
 * Only auth-specific providers belong here — everything else goes in app.config.shared.ts.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    ...commonProviders,
    provideHttpClient(...sharedHttpFeatures(), withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
  ],
};
