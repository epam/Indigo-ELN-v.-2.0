import { mergeEnvironments } from './env.util';

export const environment = mergeEnvironments({
  production: false,
  authProvider: 'keycloak',
  keycloak: {
    url: 'http://localhost:8088',
    realm: 'indigo-eln',
    clientId: 'frontend-client',
  },
  authConfig: {
    userPoolId: '',
    userPoolClientId: '',
    identityPoolId: '',
  },
});