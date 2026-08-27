import { describe, expect, it, vi } from 'vitest';

vi.mock('aws-amplify/auth', () => ({
  fetchAuthSession: vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'test-token' } } }),
}));

const { ApiError, apiFetch } = await import('@/lib/api');

describe('apiFetch', () => {
  it('sends the path untouched with the bearer token attached', async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ username: 'alice' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      }),
    );
    vi.stubGlobal('fetch', fetchMock);

    await expect(apiFetch('/api/eln/currentUser')).resolves.toEqual({ username: 'alice' });

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe('/api/eln/currentUser');
    expect((init.headers as Record<string, string>).Authorization).toBe('Bearer test-token');
  });

  it('resolves 204 to null', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(null, { status: 204 })));

    await expect(apiFetch('/api/signature/documents')).resolves.toBeNull();
  });

  it('throws ApiError carrying the status and parsed body', async () => {
    // A Response body can only be read once, so build a fresh one per call.
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => new Response(JSON.stringify({ message: 'nope' }), { status: 403 })),
    );

    await expect(apiFetch('/api/eln/currentUser')).rejects.toMatchObject({
      name: 'ApiError',
      status: 403,
      body: { message: 'nope' },
    });
    await expect(apiFetch('/api/eln/currentUser')).rejects.toBeInstanceOf(ApiError);
  });

  it('returns the raw body as text and accepts anything when asked for text', async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValue(new Response('<svg/>', { status: 200, headers: { 'Content-Type': 'image/svg+xml' } }));
    vi.stubGlobal('fetch', fetchMock);

    await expect(apiFetch('/api/eln/experiments/1/image', { responseType: 'text' })).resolves.toBe('<svg/>');

    const [, init] = fetchMock.mock.calls[0];
    expect((init.headers as Record<string, string>).Accept).toBe('*/*');
  });

  it('returns a Blob when asked for one', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('bytes', { status: 200 })));

    const blob = await apiFetch('/api/eln/users/alice/avatar', { responseType: 'blob' });

    expect(blob).toBeInstanceOf(Blob);
    await expect(blob.text()).resolves.toBe('bytes');
  });

  it('rejects rather than falling back to text when a JSON body is malformed', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('<svg/>', { status: 200 })));

    await expect(apiFetch('/api/eln/currentUser')).rejects.toBeInstanceOf(SyntaxError);
  });

  it('still throws ApiError with the parsed JSON body on a failed text request', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(new Response(JSON.stringify([{ message: 'nope' }]), { status: 403 })),
    );

    await expect(apiFetch('/api/eln/experiments/1/image', { responseType: 'text' })).rejects.toMatchObject({
      name: 'ApiError',
      status: 403,
      body: [{ message: 'nope' }],
    });
  });
});
