import { Authenticator, useAuthenticator } from '@aws-amplify/ui-react';
import { createFileRoute, useRouter } from '@tanstack/react-router';
import { useEffect } from 'react';
import { z } from 'zod';

import '@aws-amplify/ui-react/styles.css';

export const Route = createFileRoute('/login')({
  validateSearch: z.object({
    redirect: z.string().optional(),
  }),
  component: LoginPage,
});

function LoginPage() {
  return (
    <div className="flex min-h-svh items-center justify-center p-6">
      <Authenticator hideSignUp>
        <RedirectOnSignIn />
      </Authenticator>
    </div>
  );
}

/** <Authenticator> renders its children only once a session exists. */
function RedirectOnSignIn() {
  const { redirect } = Route.useSearch();
  const router = useRouter();
  const { authStatus } = useAuthenticator((context) => [context.authStatus]);

  useEffect(() => {
    if (authStatus !== 'authenticated') return;
    // `redirect` is an arbitrary href captured by the _auth guard, not one of the
    // router's known route paths, so it goes through history rather than navigate().
    router.history.replace(redirect ?? '/projects');
  }, [authStatus, redirect, router]);

  return null;
}
