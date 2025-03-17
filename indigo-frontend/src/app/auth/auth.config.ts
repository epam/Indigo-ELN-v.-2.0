import { environment } from '@/environments/environment';
import { PassedInitialConfig } from 'angular-auth-oidc-client';

export const authConfig: PassedInitialConfig = {
  config: environment.authConfig,
};
