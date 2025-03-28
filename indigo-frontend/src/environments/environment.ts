import { mergeEnvironments } from './env.util';

export const environment = mergeEnvironments({
  production: false,
  authConfig: {
    postLogoutRedirectUri: window.location.origin,
    authority:
      'https://cognito-idp.us-east-1.amazonaws.com/us-east-1_6DirgtQ1p',
    redirectUrl: 'http://localhost:4200',
    clientId: 'a4kkh00ob23l7upm88hh3mmj5',
  },
});
