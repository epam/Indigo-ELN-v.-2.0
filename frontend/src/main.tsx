import { createRouter, RouterProvider } from '@tanstack/react-router';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

import { RouteError } from '@/components/common/route-error';
import { auth, initAuth } from '@/lib/auth';
import { QueryPersistenceProvider } from '@/lib/query-persistence';
import { routeTree } from '@/routeTree.gen';
import '@/styles.css';

// Loads config.json, selects the identity provider it names, and finishes that provider's
// setup — under Keycloak that includes the check-sso round trip, so the session read below
// is already resolved by the time anything uses it.
await initAuth();

// defaultErrorComponent, not the root route's errorComponent: without one, TanStack mounts
// no boundary at all and a render throw blanks the whole app. See route-error.tsx.
const router = createRouter({
  routeTree,
  defaultErrorComponent: RouteError,
});

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
const userSub = await auth.getUserId();

createRoot(rootElement).render(
  <StrictMode>
    <QueryPersistenceProvider initialUserSub={userSub}>
      <RouterProvider router={router} />
    </QueryPersistenceProvider>
  </StrictMode>,
);
