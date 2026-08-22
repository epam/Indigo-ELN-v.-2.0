import { Toast } from '@base-ui/react/toast';

import { ApiError } from '@/lib/api';

/**
 * Created outside React so `apiFetch` can raise a toast without a hook. The provider
 * in `__root.tsx` binds it to the rendered viewport.
 */
export const toastManager = Toast.createToastManager();

/** The shape the backend uses for bean-validation failures. */
interface BackendError {
  path?: string;
  message: string;
}

function isBackendErrorArray(body: unknown): body is BackendError[] {
  return Array.isArray(body) && body.every((entry) => typeof entry?.message === 'string');
}

/**
 * Port of detectMessage() in indigo-frontend's error.interceptor.ts: returns the
 * message to show, and the line to log next to it.
 */
export function describeError(error: unknown, url?: string): [message: string, log: string] {
  if (error instanceof ApiError) {
    const log = `Server error calling ${url ?? 'the API'}: ${error.status}`;
    if (error.status === 403) {
      const message = "You don't have permission to perform this action";
      return [message, `${log}: ${message}`];
    }
    if (isBackendErrorArray(error.body)) {
      const message = error.body.map((x) => (x.path ? `${x.path}: ${x.message}` : x.message)).join('\n');
      return [message, `${log}: ${message}`];
    }
    return ['Server error. Please try again later', log];
  }
  if (error instanceof Error && error.message) {
    return [error.message, error.message];
  }
  return ['Unknown error', 'Unknown error'];
}

/**
 * Toasts a failed request. Called from `apiFetch` so every endpoint reports failures
 * the same way, mirroring the Angular interceptor — which also re-throws, leaving
 * callers free to handle the error as well.
 */
export function notifyError(error: unknown, url?: string): void {
  const [message, log] = describeError(error, url);
  console.error(log, error);
  toastManager.add({ title: message, type: 'error' });
}
