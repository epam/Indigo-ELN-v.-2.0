import { createMemoryHistory, createRouter } from '@tanstack/react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const getAccessToken = vi.fn();
const requireLogin = vi.fn();
vi.mock('@/lib/auth', () => ({ auth: { getAccessToken, requireLogin, signOut: vi.fn() } }));

const { routeTree } = await import('@/routeTree.gen');

function routerAt(path: string) {
  return createRouter({ routeTree, history: createMemoryHistory({ initialEntries: [path] }) });
}

describe('_auth guard', () => {
  beforeEach(() => {
    getAccessToken.mockReset();
    // The Cognito answer: the provider has not taken over, so the guard routes to /login itself.
    requireLogin.mockReset().mockResolvedValue(false);
  });

  it('sends unauthenticated visitors to /login with a redirect back', async () => {
    getAccessToken.mockResolvedValue(undefined);

    const router = routerAt('/projects');
    await router.load();

    expect(router.state.location.pathname).toBe('/login');
    expect(router.state.location.search).toEqual({ redirect: '/projects' });
  });

  it('leaves the redirect to the provider when it handles the login itself', async () => {
    // What Keycloak does: requireLogin navigates to the hosted login page, so the guard must not
    // also send the user to the in-app /login form, which that environment does not render.
    getAccessToken.mockResolvedValue(undefined);
    requireLogin.mockResolvedValue(true);

    const router = routerAt('/projects');
    await router.load();

    expect(requireLogin).toHaveBeenCalledWith('/projects');
    expect(router.state.location.pathname).not.toBe('/login');
  });

  it('lets an authenticated visitor through to the guarded route', async () => {
    getAccessToken.mockResolvedValue('token');

    const router = routerAt('/projects');
    await router.load();

    expect(router.state.location.pathname).toBe('/projects');
    expect(requireLogin).not.toHaveBeenCalled();
  });

  it('redirects the index route to /projects', async () => {
    getAccessToken.mockResolvedValue('token');

    const router = routerAt('/');
    await router.load();

    expect(router.state.location.pathname).toBe('/projects');
  });
});
