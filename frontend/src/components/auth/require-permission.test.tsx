import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  createMemoryHistory,
  createRootRoute,
  createRoute,
  createRouter,
  RouterProvider,
} from '@tanstack/react-router';
import { render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import type { CurrentUser } from '@/lib/types/user';

// Mocked at the transport layer: useCurrentUser calls apiFetch directly, so stubbing
// the module's own fetchCurrentUser export would not intercept it.
const apiFetch = vi.fn<() => Promise<CurrentUser>>();
vi.mock('@/lib/api', () => ({ apiFetch: () => apiFetch() }));

const { RequirePermission } = await import('@/components/auth/require-permission');

function user(permissions: CurrentUser['permissions']): CurrentUser {
  return { id: 'u1', username: 'anna', displayName: 'Anna', permissions };
}

/**
 * The guard renders inside a two-route memory router so the redirect it performs has
 * somewhere to land, mirroring the real tree where '/' forwards on to /projects.
 */
function renderGuarded() {
  const rootRoute = createRootRoute();
  const homeRoute = createRoute({ getParentRoute: () => rootRoute, path: '/', component: () => <p>home</p> });
  const guardedRoute = createRoute({
    getParentRoute: () => rootRoute,
    path: '/dictionaries',
    component: () => <RequirePermission permission="MANAGE_DICTIONARIES">secret</RequirePermission>,
  });

  const router = createRouter({
    routeTree: rootRoute.addChildren([homeRoute, guardedRoute]),
    history: createMemoryHistory({ initialEntries: ['/dictionaries'] }),
  });

  render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { queries: { retry: false } } })}>
      <RouterProvider router={router as never} />
    </QueryClientProvider>,
  );

  return router;
}

describe('RequirePermission', () => {
  it('renders the children once the permission is confirmed', async () => {
    apiFetch.mockResolvedValue(user(['MANAGE_DICTIONARIES']));

    renderGuarded();

    expect(await screen.findByText('secret')).toBeInTheDocument();
  });

  it('redirects to the root page when the permission is missing', async () => {
    apiFetch.mockResolvedValue(user(['VIEW_PROJECTS']));

    const router = renderGuarded();

    await waitFor(() => expect(router.state.location.pathname).toBe('/'));
    expect(screen.queryByText('secret')).not.toBeInTheDocument();
  });

  it('shows nothing and stays put while currentUser is still loading', async () => {
    apiFetch.mockReturnValue(new Promise(() => {}));

    const router = renderGuarded();

    await waitFor(() => expect(screen.queryByText('secret')).not.toBeInTheDocument());
    expect(router.state.location.pathname).toBe('/dictionaries');
  });
});
