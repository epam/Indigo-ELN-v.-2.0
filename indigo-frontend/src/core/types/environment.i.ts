import { type PassedInitialConfig } from 'angular-auth-oidc-client';

export interface IEnvironment {
  production: boolean;
  authConfig: PassedInitialConfig['config'];
}
