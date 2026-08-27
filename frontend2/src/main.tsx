import { createRouter, RouterProvider } from '@tanstack/react-router';
import { fetchAuthSession } from 'aws-amplify/auth';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

import { configureAmplify } from '@/lib/amplify';
import { QueryPersistenceProvider } from '@/lib/query-persistence';
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

// Resolved before the first render so a page opened by an already signed-in user starts
// restoring immediately, rather than fetching its sidebar chrome and then being handed the
// cached copy a moment later. Signing in later is picked up by the provider itself.
const session = await fetchAuthSession().catch(() => null);

createRoot(rootElement).render(
  <StrictMode>
    <QueryPersistenceProvider initialUserSub={session?.userSub}>
      <RouterProvider router={router} />
    </QueryPersistenceProvider>
  </StrictMode>,
);
