import { Authenticator, useAuthenticator } from '@aws-amplify/ui-react';
import { createFileRoute, useRouter } from '@tanstack/react-router';
import { useEffect } from 'react';

import { z } from '@/lib/zod';

// `?inline` hands the stylesheet over as a string instead of injecting it, which is what lets
// this route mount and unmount it. See `useAmplifyStyles` below.
import amplifyStyles from '@/amplify-styles.css?inline';

export const Route = createFileRoute('/login')({
  validateSearch: z.object({
    redirect: z.string().optional(),
  }),
  component: LoginPage,
});

/**
 * Mounts Amplify's stylesheet for as long as this screen is on, and takes it away again.
 *
 * A plain `import '…css'` cannot do that: the bundler injects it when the route chunk loads and
 * nothing ever removes it, so after one visit to `/login` it applies to every screen for the life
 * of the tab. That is not academic — two of its rules are global, and
 * `input, button, textarea, select { font: inherit }` reset the font size of every form control
 * in the app, which is how a table cell ended up ignoring its own `text-[13px]` class.
 *
 * This route renders alone rather than inside `AppShell`, so while the sheet is mounted there is
 * nothing of ours for it to reach. `amplify-styles.css` also wraps it in a cascade layer, and
 * that stays: it is the belt to this brace, and it is what still protects the app if this ever
 * fails to clean up — an error thrown mid-unmount, say.
 */
function useAmplifyStyles() {
  useEffect(() => {
    const style = document.createElement('style');
    style.dataset.amplify = '';
    style.textContent = amplifyStyles;
    document.head.append(style);
    return () => style.remove();
  }, []);
}

function LoginPage() {
  useAmplifyStyles();

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
