import { Amplify } from 'aws-amplify';

import { env } from '@/lib/env';

/** Called once from main.tsx, before anything touches `fetchAuthSession`. */
export function configureAmplify() {
  Amplify.configure({
    Auth: {
      Cognito: {
        userPoolId: env.VITE_COGNITO_USER_POOL_ID,
        userPoolClientId: env.VITE_COGNITO_CLIENT_ID,
      },
    },
  });
}
