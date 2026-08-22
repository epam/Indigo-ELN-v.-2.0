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
 * Mirrors the convention in indigo-frontend/src/core/services/api.service.ts:
 * bare paths resolve against the ELN API, fully-qualified /api paths pass through.
 */
function buildUrl(path: string): string {
  return path.startsWith('/api/') ? path : `/api/eln/${path.replace(/^\/+/, '')}`;
}

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

async function parseBody(response: Response): Promise<unknown> {
  if (response.status === 204) return null;
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  const url = buildUrl(path);
  const response = await fetch(url, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init.body ? { 'Content-Type': 'application/json' } : {}),
      ...(await authHeader()),
      ...init.headers,
    },
  });

  const body = await parseBody(response);
  if (!response.ok) {
    const error = new ApiError(response.status, body);
    // Toasted here rather than per-caller, mirroring indigo-frontend's error.interceptor.ts.
    // Still thrown, so TanStack Query and the Collection error branch keep working.
    notifyError(error, url);
    throw error;
  }
  return body as T;
}
