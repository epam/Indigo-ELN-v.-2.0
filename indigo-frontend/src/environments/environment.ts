import { mergeEnvironments } from './env.util';

export const environment = mergeEnvironments({
  production: false,
  authProvider: 'cognito',
  keycloak: {
    url: '',
    realm: '',
    clientId: '',
  },
  authConfig: {
    userPoolId: 'us-east-1_6DirgtQ1p',
    userPoolClientId: 'a4kkh00ob23l7upm88hh3mmj5',
    identityPoolId: '',
  },
});
