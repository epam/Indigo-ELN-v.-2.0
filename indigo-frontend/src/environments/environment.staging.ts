import { IEnvironment } from '@/core/types/environment.i';
import { mergeEnvironments } from './env.util';

export const environment: IEnvironment = mergeEnvironments({
  production: false,
  authProvider: 'cognito',
  keycloak: {
    url: '',
    realm: '',
    clientId: '',
  },
  authConfig: {
    userPoolId: '',
    userPoolClientId: '',
    identityPoolId: '',
  },
});
