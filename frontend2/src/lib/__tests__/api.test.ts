import { describe, expect, it, vi } from 'vitest';

vi.mock('aws-amplify/auth', () => ({
  fetchAuthSession: vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'test-token' } } }),
}));

const { ApiError, apiFetch } = await import('@/lib/api');

describe('apiFetch', () => {
  it('prefixes bare paths with the ELN base and sends the bearer token', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ username: 'alice' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    );
    vi.stubGlobal('fetch', fetchMock);

    await expect(apiFetch('currentUser')).resolves.toEqual({ username: 'alice' });

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe('/api/eln/currentUser');
    expect((init.headers as Record<string, string>).Authorization).toBe('Bearer test-token');
  });

  it('passes through fully-qualified /api paths untouched', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(null, { status: 204 }));
    vi.stubGlobal('fetch', fetchMock);

    await apiFetch('/api/signature/documents');

    expect(fetchMock.mock.calls[0][0]).toBe('/api/signature/documents');
  });

  it('throws ApiError carrying the status and parsed body', async () => {
    // A Response body can only be read once, so build a fresh one per call.
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => new Response(JSON.stringify({ message: 'nope' }), { status: 403 })),
    );

    await expect(apiFetch('currentUser')).rejects.toMatchObject({
      name: 'ApiError',
      status: 403,
      body: { message: 'nope' },
    });
    await expect(apiFetch('currentUser')).rejects.toBeInstanceOf(ApiError);
  });
});
