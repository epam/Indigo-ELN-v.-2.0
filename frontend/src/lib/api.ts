import { auth } from '@/lib/auth';

/** The request an `ApiError` answered — what its toast logs and its Report Error action attaches. */
export interface FailedRequest {
  path: string;
  method: string;
  json: unknown;
}

/** Thrown for any non-2xx response so TanStack Query can surface it. */
export class ApiError extends Error {
  readonly status: number;
  readonly body: unknown;
  /** Absent only for one built by hand, as the toast stories do. */
  readonly request?: FailedRequest;

  constructor(status: number, body: unknown, request?: FailedRequest) {
    super(`Request failed with status ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
    this.request = request;
  }
}

/**
 * How to read a successful body. The caller declares it; `apiFetch` never guesses.
 * `json` is the default and now *fails* on a malformed body rather than quietly
 * handing back the raw text.
 */
type ResponseType = 'json' | 'text' | 'blob';

type ApiRequestInit = Omit<RequestInit, 'body'> & {
  responseType?: ResponseType;
  /** Serialised here, and what puts `Content-Type: application/json` on the request. */
  json?: unknown;
  /**
   * A multipart upload — the attachment endpoints and `importSDF`. Handed to `fetch` as-is so
   * the browser serialises it and sets its own `Content-Type`: only the browser knows the
   * boundary it generated, and naming the type without one makes the body unparseable.
   */
  formData?: FormData;
};

/**
 * The backend resolves the principal from the first of these claims it finds:
 * `preferred_username` (Keycloak), then `username` (a Cognito *access* token — an ID token
 * spells it `cognito:username`), then the principal name. So both providers must hand back an
 * access token, never an ID token. See UserHolder.java.
 */
async function authHeader(): Promise<Record<string, string>> {
  const token = await auth.getAccessToken();
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function parseSuccessBody(response: Response, responseType: ResponseType): Promise<unknown> {
  if (responseType === 'blob') return response.blob();
  if (responseType === 'text') return response.text();
  if (response.status === 204) return null;
  const text = await response.text();
  if (!text) return null;
  return JSON.parse(text);
}

/**
 * Errors are read the same way whatever the caller asked for: the backend reports them
 * as the JSON bean-validation shape `describeError` expects, and a failed image request
 * has to produce a readable toast rather than a Blob. Non-JSON bodies (a proxy's HTML
 * error page) still come through as text so the log line carries something.
 */
async function parseErrorBody(response: Response): Promise<unknown> {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

/**
 * The request itself: headers, the call, and the non-2xx contract. Split out of `apiFetch` so
 * `apiDownload` can reach the `Response` — it names the saved file from a *header*, which a
 * parsed body cannot carry — without either of them duplicating auth or error handling.
 *
 * **Nothing here toasts.** A failure is reported where TanStack sees it — the `QueryCache` and
 * `MutationCache` `onError` in `query-client.ts` — which also catches what never was a response:
 * a network failure, an unparseable body, an `onSuccess` that threw. A call made outside a query
 * or a mutation reports its own failure with `notifyError`.
 */
async function apiRequest(path: string, init: ApiRequestInit = {}): Promise<Response> {
  const { responseType = 'json', json, formData, ...requestInit } = init;
  const response = await send(path, {
    ...requestInit,
    body: json === undefined ? formData : JSON.stringify(json),
    headers: {
      // The image endpoints declare a concrete `@Produces` (`image/svg+xml`, `image/png`),
      // so anything but JSON has to accept a wildcard or the backend answers 406.
      Accept: responseType === 'json' ? 'application/json' : '*/*',
      // Named by `json` alone, so a multipart upload keeps the boundary-carrying type the
      // browser generates for it.
      ...(json !== undefined ? { 'Content-Type': 'application/json' } : {}),
      ...(await authHeader()),
      ...requestInit.headers,
    },
  });

  if (!response.ok) {
    throw new ApiError(response.status, await parseErrorBody(response), {
      path,
      method: requestInit.method ?? 'GET',
      json,
    });
  }
  return response;
}

/**
 * `fetch`, with a rejection that reads as one: the browser's own is a bare `TypeError: Failed to
 * fetch` for a dropped connection, a CSP block and a proxy reset alike. An abort is passed through
 * untouched — TanStack Query aborts a query nobody is watching, and that is not a failure.
 */
async function send(path: string, init: RequestInit): Promise<Response> {
  try {
    return await fetch(path, init);
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') throw error;
    throw new Error('Could not reach the server', { cause: error });
  }
}

export function apiFetch<T>(path: string, init?: ApiRequestInit & { responseType?: 'json' }): Promise<T>;
export function apiFetch(path: string, init: ApiRequestInit & { responseType: 'text' }): Promise<string>;
export function apiFetch(path: string, init: ApiRequestInit & { responseType: 'blob' }): Promise<Blob>;
export async function apiFetch(path: string, init: ApiRequestInit = {}): Promise<unknown> {
  const response = await apiRequest(path, init);
  return parseSuccessBody(response, init.responseType ?? 'json');
}

/**
 * The filename a download endpoint asked for, or `undefined` when it did not say.
 *
 * Exported only so the header shapes can be table-tested directly rather than through a stubbed
 * `fetch` per case. The backend sends *both* parameters — see `ContentDispositionUtil.java`:
 *
 *     attachment; filename="report.pdf"; filename*=UTF-8''report.pdf
 *
 * `filename*` is preferred, as RFC 6266 requires and because it is the only form that survives a
 * non-ASCII name: the plain parameter carries raw UTF-8 bytes, which `Headers.get()` hands back
 * mis-decoded. No `content-disposition` package for this —
 * three regexes against a header this same server generates is not worth one.
 *
 * Each pattern is anchored on `(?:^|;)\s*filename`, or the plain-`filename` one would also match
 * inside `filename*=`.
 */
const FILENAME_EXTENDED = /(?:^|;)\s*filename\*\s*=\s*[^']*'[^']*'([^;]+)/i;
const FILENAME_QUOTED = /(?:^|;)\s*filename\s*=\s*"((?:[^"\\]|\\.)*)"/i;
const FILENAME_BARE = /(?:^|;)\s*filename\s*=\s*([^;]+)/i;

export function filenameFromContentDisposition(header: string | null): string | undefined {
  if (!header) return undefined;

  const extended = FILENAME_EXTENDED.exec(header);
  if (extended) {
    try {
      return decodeURIComponent(extended[1].trim());
    } catch {
      // A malformed percent-encoding falls through to the plain parameter rather than throwing.
    }
  }

  const quoted = FILENAME_QUOTED.exec(header);
  if (quoted) return quoted[1].replace(/\\(.)/g, '$1');

  return FILENAME_BARE.exec(header)?.[1].trim() || undefined;
}

/**
 * Saves a download to disk, named by the server when it says so and by `fallbackFilename` when it
 * does not.
 *
 * These endpoints require the Cognito bearer token, so a plain `<a href download>` would 401 — the
 * bytes have to be fetched first and handed to a synthetic anchor. The object URL is revoked
 * immediately: the click is synchronous, so the browser has already taken its reference.
 *
 * `Content-Disposition` is not CORS-safelisted, so it is readable only because `/api` is
 * same-origin (both the dev proxy and CloudFront). Were that to change it would silently read
 * `null`, and the fallback would carry the download — the other reason the fallback exists.
 *
 * Rethrows, like `apiFetch`: a transport function that quietly resolved on error would be the
 * odd one out here. Reporting and swallowing belong to the caller — `useDownload` does both.
 *
 * `init` is for the downloads that are not plain GETs — the experiment report is a `POST`. It is
 * spread *before* `responseType`, so a caller cannot ask for a parsed body from a function whose
 * whole job is to hand the bytes to an anchor.
 */
export async function apiDownload(path: string, fallbackFilename: string, init: ApiRequestInit = {}): Promise<void> {
  const response = await apiRequest(path, { ...init, responseType: 'blob' });
  const filename = filenameFromContentDisposition(response.headers.get('Content-Disposition'));

  const url = URL.createObjectURL(await response.blob());
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename ?? fallbackFilename;
  anchor.click();
  URL.revokeObjectURL(url);
}
