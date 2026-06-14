import { mergeEnvironments } from './env.util';

export const environment = mergeEnvironments({
  production: true,
  authProvider: 'cognito',
  keycloak: {
    url: '',
    realm: '',
    clientId: '',
  },
  authConfig: {
    userPoolId: 'us-east-1_bgZNcyeDz',
    userPoolClientId: '3bqu2iod15b0otft0ap8umlfgh',
    identityPoolId: '',
  },
});
