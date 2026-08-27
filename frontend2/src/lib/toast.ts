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

/** Title of the 401 toast; also its id, since the id is what collapses a burst of them. */
const AUTH_FAILED = 'Authorization failed';

/**
 * The readable part of an error body, if it has one. Worth surfacing for a 401 in
 * particular, because the detail is the only thing separating the two very different
 * causes: API Gateway's authorizer answers `{"message":"Unauthorized"}` when the token is
 * missing or expired, while `APISecretFilter` answers the plain string `Invalid API
 * secret` when the request did not arrive through CloudFront — a deployment or dev-proxy
 * problem rather than anything the signed-in user did.
 */
function errorDetail(body: unknown): string | undefined {
  if (typeof body === 'string') return body.trim() || undefined;
  if (body !== null && typeof body === 'object' && 'message' in body && typeof body.message === 'string') {
    return body.message.trim() || undefined;
  }
  return undefined;
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
    if (error.status === 401) {
      const detail = errorDetail(error.body);
      return [AUTH_FAILED, `${log}: ${detail ?? AUTH_FAILED}`];
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

  if (error instanceof ApiError && error.status === 401) {
    toastManager.add({
      // A fixed id is the whole dedup story: Base UI updates a toast in place when one
      // with the same id is added, so the burst of 401s a screenful of parallel queries
      // produces reads as the single problem it is.
      id: AUTH_FAILED,
      title: message,
      description: errorDetail(error.body),
      type: 'error',
      // No auto-dismiss: the action below is the point of this toast, and a 401 does not
      // stop being true after five seconds.
      timeout: 0,
      actionProps: {
        children: 'Reload',
        // Reloading is enough to recover, because `_auth`'s beforeLoad guard re-runs and
        // sends an ended session to /login. A 401 that was really a misconfigured proxy
        // simply reports itself again, rather than bouncing the user out.
        onClick: () => window.location.reload(),
      },
    });
    return;
  }

  toastManager.add({ title: message, type: 'error' });
}
