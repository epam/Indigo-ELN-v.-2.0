import { describe, expect, it } from 'vitest';

import { ApiError } from '@/lib/api';
import { describeError } from '@/lib/toast';

/** Cases lifted from detectMessage() in indigo-frontend's error.interceptor.ts. */
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
