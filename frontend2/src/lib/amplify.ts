import { Amplify } from 'aws-amplify';

import { applyStoredSessionPersistence } from '@/lib/auth-storage';
import { loadConfig } from '@/lib/env';

/** Called once from main.tsx, before anything touches `fetchAuthSession`. */
export async function configureAmplify() {
  const config = await loadConfig();
  Amplify.configure({
    Auth: {
      Cognito: {
        userPoolId: config.cognitoUserPoolId,
        userPoolClientId: config.cognitoClientId,
      },
    },
  });
  // Ordering is load-bearing: main.tsx resolves a session immediately after this call, and that
  // read has to reach the store the tokens were actually written to. See auth-storage.ts.
  applyStoredSessionPersistence();
}
