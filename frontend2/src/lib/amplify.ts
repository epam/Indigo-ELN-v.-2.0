import { Amplify } from 'aws-amplify';

import { applyStoredSessionPersistence } from '@/lib/auth-storage';
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
  // Ordering is load-bearing: main.tsx resolves a session immediately after this call, and that
  // read has to reach the store the tokens were actually written to. See auth-storage.ts.
  applyStoredSessionPersistence();
}
