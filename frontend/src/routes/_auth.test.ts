import { createMemoryHistory, createRouter } from '@tanstack/react-router';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const fetchAuthSession = vi.fn();
vi.mock('aws-amplify/auth', () => ({ fetchAuthSession, signOut: vi.fn() }));

const { routeTree } = await import('@/routeTree.gen');

function routerAt(path: string) {
  return createRouter({ routeTree, history: createMemoryHistory({ initialEntries: [path] }) });
}

describe('_auth guard', () => {
  beforeEach(() => {
    fetchAuthSession.mockReset();
  });

  it('sends unauthenticated visitors to /login with a redirect back', async () => {
    fetchAuthSession.mockResolvedValue({ tokens: undefined });

    const router = routerAt('/projects');
    await router.load();

    expect(router.state.location.pathname).toBe('/login');
    expect(router.state.location.search).toEqual({ redirect: '/projects' });
  });

  it('lets an authenticated visitor through to the guarded route', async () => {
    fetchAuthSession.mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });

    const router = routerAt('/projects');
    await router.load();

    expect(router.state.location.pathname).toBe('/projects');
  });

  it('redirects the index route to /projects', async () => {
    fetchAuthSession.mockResolvedValue({ tokens: { accessToken: { toString: () => 'token' } } });

    const router = routerAt('/');
    await router.load();

    expect(router.state.location.pathname).toBe('/projects');
  });
});
