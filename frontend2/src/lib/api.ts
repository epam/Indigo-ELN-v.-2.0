import { fetchAuthSession } from 'aws-amplify/auth';

import { notifyError } from '@/lib/toast';

/** Thrown for any non-2xx response so TanStack Query can surface it. */
export class ApiError extends Error {
  readonly status: number;
  readonly body: unknown;

  constructor(status: number, body: unknown) {
    super(`Request failed with status ${status}`);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
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
 * The backend resolves the principal from the `username` claim, which only exists
 * on a Cognito *access* token (an ID token spells it `cognito:username`), so this
 * must stay accessToken. See UserHolder.java and indigo-frontend's jwt.interceptor.
 */
async function authHeader(): Promise<Record<string, string>> {
  const session = await fetchAuthSession();
  const accessToken = session.tokens?.accessToken;
  return accessToken ? { Authorization: `Bearer ${accessToken.toString()}` } : {};
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
 */
async function apiRequest(path: string, init: ApiRequestInit = {}): Promise<Response> {
  const { responseType = 'json', json, formData, ...requestInit } = init;
  const response = await fetch(path, {
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
    const error = new ApiError(response.status, await parseErrorBody(response));
    // Toasted here rather than per-caller, mirroring indigo-frontend's error.interceptor.ts.
    // Still thrown, so TanStack Query and the InfiniteLoader error branch keep working.
    notifyError(error, path);
    throw error;
  }
  return response;
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
 * mis-decoded. No `content-disposition` package for this (indigo-frontend took the dependency) —
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
 * Rethrows, like `apiFetch` and for the same reason: the failure is already toasted, but a
 * transport function that quietly resolved on error would be the odd one out here. Swallowing
 * belongs to the caller — `useDownload` does it.
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
