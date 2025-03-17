import { mergeEnvironments } from './env.util';

export const environment = mergeEnvironments({
  production: false,
  authConfig: {
    postLogoutRedirectUri: window.location.origin,
    authority:
      'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_GnMjXfy1G',
    redirectUrl: 'http://localhost:4200',
    clientId: '2a4sr216nlm9me536ev8tic5uk',
  },
});
