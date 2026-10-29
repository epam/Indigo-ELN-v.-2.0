import { afterEach, describe, expect, it, vi } from 'vitest';

import { ApiError } from '@/lib/api';
import { INCIDENTS_PATH } from '@/lib/report-error';
import { describeError, notifyError, toastManager } from '@/lib/toast';

describe('describeError', () => {
  it('names the missing permission on 403', () => {
    const [message] = describeError(new ApiError(403, null), '/api/eln/projects');
    expect(message).toBe("You don't have permission to perform this action");
  });

  it('reports a 401 as an authorization failure whatever produced it', () => {
    // API Gateway's user-pool authorizer: the token was missing, expired or revoked.
    const [message, log] = describeError(new ApiError(401, { message: 'Unauthorized' }), '/api/eln/projects');
    expect(message).toBe('Authorization failed');
    expect(log).toBe('Server error calling /api/eln/projects: 401: Unauthorized');
  });

  it('carries a plain-text 401 body through to the log', () => {
    // APISecretFilter: the request did not arrive via CloudFront, so this is a proxy or
    // deployment problem — the detail is what tells the two 401s apart.
    const [message, log] = describeError(new ApiError(401, 'Invalid API secret'), '/api/eln/projects');
    expect(message).toBe('Authorization failed');
    expect(log).toBe('Server error calling /api/eln/projects: 401: Invalid API secret');
  });

  it('still logs something for a 401 with no body at all', () => {
    const [, log] = describeError(new ApiError(401, null), '/api/eln/projects');
    expect(log).toBe('Server error calling /api/eln/projects: 401: Authorization failed');
  });

  it('joins a bean-validation body into one message per field', () => {
    const body = [{ path: 'name', message: 'must not be empty' }, { message: 'project is invalid' }];
    const [message] = describeError(new ApiError(400, body));
    expect(message).toBe('name: must not be empty\nproject is invalid');
  });

  it('falls back to a generic message for any other server error', () => {
    const [message] = describeError(new ApiError(500, 'Internal Server Error'));
    expect(message).toBe('Server error. Please try again later');
  });

  it('logs the URL and status alongside a generic failure', () => {
    const [, log] = describeError(new ApiError(500, null), '/api/eln/projects');
    expect(log).toBe('Server error calling /api/eln/projects: 500');
  });

  it('uses the message of a plain Error', () => {
    const [message] = describeError(new Error('Failed to fetch'));
    expect(message).toBe('Failed to fetch');
  });

  it('falls back to Unknown error for a non-Error throw', () => {
    const [message] = describeError('nope');
    expect(message).toBe('Unknown error');
  });
});

describe('notifyError', () => {
  afterEach(() => vi.restoreAllMocks());

  /** The cache's `onError` passes the error alone: the request has to come from the error itself. */
  it('offers to report an ApiError, with the request it carries', () => {
    const add = vi.spyOn(toastManager, 'add');
    vi.spyOn(console, 'error').mockImplementation(() => {});

    notifyError(new ApiError(500, 'boom', { path: '/api/eln/projects', method: 'POST', json: { name: 'p' } }));

    expect(add).toHaveBeenCalledWith(
      expect.objectContaining({ actionProps: expect.objectContaining({ children: 'Report Error' }) }),
    );
    expect(console.error).toHaveBeenCalledWith('Server error calling /api/eln/projects: 500', expect.any(ApiError));
  });

  /** A failed report offering to report itself would loop. */
  it('offers no report for a failed report', () => {
    const add = vi.spyOn(toastManager, 'add');
    vi.spyOn(console, 'error').mockImplementation(() => {});

    notifyError(new ApiError(500, null, { path: INCIDENTS_PATH, method: 'POST', json: undefined }));

    expect(add).toHaveBeenCalledWith(expect.not.objectContaining({ actionProps: expect.anything() }));
  });
});
