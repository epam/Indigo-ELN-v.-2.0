import { mergeEnvironments } from './env.util';

export const environment = mergeEnvironments({
  production: false,
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
