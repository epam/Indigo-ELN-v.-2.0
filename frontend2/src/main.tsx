import { createRouter, RouterProvider } from '@tanstack/react-router';
import { fetchAuthSession } from 'aws-amplify/auth';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';

import { RouteError } from '@/components/common/route-error';
import { configureAmplify } from '@/lib/amplify';
import { QueryPersistenceProvider } from '@/lib/query-persistence';
import { routeTree } from '@/routeTree.gen';
import '@/styles.css';

configureAmplify();

// defaultErrorComponent, not the root route's errorComponent: without one, TanStack mounts
// no boundary at all and a render throw blanks the whole app. See route-error.tsx.
//
// basepath follows Vite's `base`, so the routes stay written as '/projects' while the app is
// served from /frontend2 (see vite.config.ts).
const router = createRouter({
  routeTree,
  basepath: import.meta.env.BASE_URL,
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
const session = await fetchAuthSession().catch(() => null);

createRoot(rootElement).render(
  <StrictMode>
    <QueryPersistenceProvider initialUserSub={session?.userSub}>
      <RouterProvider router={router} />
    </QueryPersistenceProvider>
  </StrictMode>,
);
