import { describe, expect, it, vi } from 'vitest';

vi.mock('aws-amplify/auth', () => ({
  fetchAuthSession: vi.fn().mockResolvedValue({ tokens: { accessToken: { toString: () => 'test-token' } } }),
}));

const { ApiError, apiDownload, apiFetch, filenameFromContentDisposition } = await import('@/lib/api');

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

  it('leaves Content-Type to the browser for a FormData body', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response('[]', { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);

    const body = new FormData();
    body.append('file', new File(['x'], 'notes.txt'));
    await apiFetch('/api/eln/projects/1/attachments', { method: 'POST', body });

    // Naming the type without the boundary the browser generated makes the body unparseable.
    const [, init] = fetchMock.mock.calls[0];
    expect((init.headers as Record<string, string>)['Content-Type']).toBeUndefined();
  });

  it('still declares JSON for an ordinary body', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response('{}', { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);

    await apiFetch('/api/eln/projects', { method: 'POST', body: JSON.stringify({ name: 'x' }) });

    const [, init] = fetchMock.mock.calls[0];
    expect((init.headers as Record<string, string>)['Content-Type']).toBe('application/json');
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

describe('filenameFromContentDisposition', () => {
  // The first row is what the backend actually sends — see ContentDispositionUtil.java.
  const cases: [name: string, header: string | null, expected: string | undefined][] = [
    [
      // Contrived: the backend always sends the same name twice, so only a header where the two
      // disagree can pin which one wins.
      'prefers filename* over filename',
      `attachment; filename="plain.pdf"; filename*=UTF-8''extended.pdf`,
      'extended.pdf',
    ],
    ['reads the shape the backend actually sends', `attachment; filename="a.pdf"; filename*=UTF-8''a.pdf`, 'a.pdf'],
    [
      'decodes the percent-encoding filename* carries',
      `attachment; filename="report final.pdf"; filename*=UTF-8''report%20final.pdf`,
      'report final.pdf',
    ],
    [
      // The plain parameter would come back mis-decoded, which is why filename* wins.
      'decodes a non-ascii filename*',
      `attachment; filename*=UTF-8''r%C3%A9sum%C3%A9.pdf`,
      'résumé.pdf',
    ],
    ['unescapes a quoted filename', String.raw`attachment; filename="say \"hi\".txt"`, 'say "hi".txt'],
    ['reads a bare filename', 'attachment; filename=report.pdf', 'report.pdf'],
    ['ignores a header with no filename at all', 'attachment', undefined],
    ['ignores inline with no filename', 'inline', undefined],
    ['treats a missing header as unsaid', null, undefined],
    [
      // A lone % is not valid percent-encoding; decodeURIComponent throws on it.
      'falls through to the plain parameter when filename* is malformed',
      `attachment; filename="report.pdf"; filename*=UTF-8''report%zz.pdf`,
      'report.pdf',
    ],
  ];

  it.each(cases)('%s', (_name, header, expected) => {
    expect(filenameFromContentDisposition(header)).toBe(expected);
  });
});

describe('apiDownload', () => {
  function setup(headers: Record<string, string> = {}) {
    const fetchMock = vi.fn().mockResolvedValue(new Response('bytes', { status: 200, headers }));
    vi.stubGlobal('fetch', fetchMock);

    // jsdom implements neither, so both have to be stubbed outright.
    const createObjectURL = vi.fn().mockReturnValue('blob:mock-url');
    const revokeObjectURL = vi.fn();
    vi.stubGlobal('URL', { ...URL, createObjectURL, revokeObjectURL });

    const anchors: HTMLAnchorElement[] = [];
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(function (this: HTMLAnchorElement) {
      anchors.push(this);
    });

    return { fetchMock, createObjectURL, revokeObjectURL, click, anchors };
  }

  it('names the saved file from Content-Disposition when the server sends one', async () => {
    const { anchors } = setup({ 'Content-Disposition': `attachment; filename*=UTF-8''server%20name.pdf` });

    await apiDownload('/api/eln/projects/1/attachments/2', 'fallback.pdf');

    expect(anchors[0].download).toBe('server name.pdf');
    expect(anchors[0].href).toBe('blob:mock-url');
  });

  it('falls back to the supplied name when the header is absent', async () => {
    const { anchors } = setup();

    await apiDownload('/api/eln/projects/1/attachments/2', 'fallback.pdf');

    expect(anchors[0].download).toBe('fallback.pdf');
  });

  it('asks for the bytes as a blob and revokes the object URL after the click', async () => {
    const { fetchMock, createObjectURL, revokeObjectURL, click } = setup();

    await apiDownload('/api/eln/projects/1/attachments/2', 'fallback.pdf');

    const [url, init] = fetchMock.mock.calls[0];
    expect(url).toBe('/api/eln/projects/1/attachments/2');
    // A blob request accepts anything: the endpoint declares application/octet-stream.
    expect((init.headers as Record<string, string>).Accept).toBe('*/*');
    expect(createObjectURL).toHaveBeenCalledOnce();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:mock-url');
    // Revoking before the click would cancel the download the click is meant to start.
    expect(click.mock.invocationCallOrder[0]).toBeLessThan(revokeObjectURL.mock.invocationCallOrder[0]);
  });

  it('rethrows a failed request rather than resolving quietly', async () => {
    setup();
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => new Response('nope', { status: 500 })),
    );

    await expect(apiDownload('/api/eln/projects/1/attachments/2', 'fallback.pdf')).rejects.toBeInstanceOf(ApiError);
  });
});
