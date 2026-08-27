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

type ApiRequestInit = RequestInit & { responseType?: ResponseType };

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

export function apiFetch<T>(path: string, init?: ApiRequestInit & { responseType?: 'json' }): Promise<T>;
export function apiFetch(path: string, init: ApiRequestInit & { responseType: 'text' }): Promise<string>;
export function apiFetch(path: string, init: ApiRequestInit & { responseType: 'blob' }): Promise<Blob>;
export async function apiFetch(path: string, init: ApiRequestInit = {}): Promise<unknown> {
  const { responseType = 'json', ...requestInit } = init;
  const response = await fetch(path, {
    ...requestInit,
    headers: {
      // The image endpoints declare a concrete `@Produces` (`image/svg+xml`, `image/png`),
      // so anything but JSON has to accept a wildcard or the backend answers 406.
      Accept: responseType === 'json' ? 'application/json' : '*/*',
      ...(requestInit.body ? { 'Content-Type': 'application/json' } : {}),
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
  return parseSuccessBody(response, responseType);
}
