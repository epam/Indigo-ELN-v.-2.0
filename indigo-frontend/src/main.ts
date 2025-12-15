import { bootstrapApplication } from '@angular/platform-browser';
import { Amplify } from 'aws-amplify';
import { I18n } from 'aws-amplify/utils';
import { AppComponent } from './app/app.component';
import { appConfig } from './app/app.config';
import { environment } from './environments/environment';

Amplify.configure({
  Auth: {
    Cognito: {
      ...environment.authConfig,
      loginWith: {
        email: false,
        username: true,
      },
      signUpVerificationMethod: 'code',
      userAttributes: {},
      allowGuestAccess: false,
      passwordFormat: {
        minLength: 8,
        requireLowercase: true,
        requireUppercase: true,
        requireNumbers: true,
        requireSpecialCharacters: true,
      },
    },
  },
});

I18n.putVocabulariesForLanguage('en', {
  'Sign in': 'Log in'
});

I18n.setLanguage('en');

bootstrapApplication(AppComponent, appConfig).catch((err) =>
  console.error(err),
);
