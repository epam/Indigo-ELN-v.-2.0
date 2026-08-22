import { QueryClientProvider } from '@tanstack/react-query';
import { PersistQueryClientProvider } from '@tanstack/react-query-persist-client';
import { createRouter, RouterProvider } from '@tanstack/react-router';
import { fetchAuthSession } from 'aws-amplify/auth';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

import { configureAmplify } from '@/lib/amplify';
import { persistOptions, queryClient } from '@/lib/query-client';
import { routeTree } from '@/routeTree.gen';
import '@/styles.css';

configureAmplify();

const router = createRouter({ routeTree });

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}

const rootElement = document.getElementById('root');
if (!rootElement) {
  throw new Error('Root element #root not found');
}

// The persisted cache is keyed by the signed-in user, so it can only be set up once the
// session is known. Signed out there is nothing to restore and nothing safe to write —
// persisting under a shared key is exactly the cross-user leak the scoped key prevents.
const session = await fetchAuthSession().catch(() => null);
const userSub = session?.userSub;

const app = <RouterProvider router={router} />;

createRoot(rootElement).render(
  <StrictMode>
    {userSub ? (
      <PersistQueryClientProvider client={queryClient} persistOptions={persistOptions(userSub)}>
        {app}
      </PersistQueryClientProvider>
    ) : (
      <QueryClientProvider client={queryClient}>{app}</QueryClientProvider>
    )}
  </StrictMode>,
);
