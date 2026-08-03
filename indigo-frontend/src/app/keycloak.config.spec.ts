import { API_BEARER_TOKEN_CONDITION, buildKeycloakOptions, SESSION_TIMEOUT_MS } from './keycloak.config';

describe('keycloak.config', () => {
  describe('API_BEARER_TOKEN_CONDITION', () => {
    const matches = (url: string) => API_BEARER_TOKEN_CONDITION.urlPattern.test(url);

    it('should match ELN backend endpoints', () => {
      expect(matches('/api/eln/currentUser')).toBe(true);
      expect(matches('/api/signature/signatures')).toBe(true);
      expect(matches('/internalapi/eln/something')).toBe(true);
      expect(matches('http://localhost/api/eln/currentUser')).toBe(true);
      expect(matches('https://indigo-eln.example.com/api/eln/currentUser')).toBe(true);
    });

    it('should not match anything outside the backend endpoints', () => {
      expect(matches('/assets/logo.svg')).toBe(false);
      expect(matches('/main.js')).toBe(false);
      expect(matches('/api-docs')).toBe(false);
      expect(matches('https://cdn.example.com/apidocs')).toBe(false);
    });
  });

  describe('buildKeycloakOptions', () => {
    const options = buildKeycloakOptions(
      { url: 'http://localhost:8088', realm: 'indigo-eln', clientId: 'frontend-client' },
      'http://localhost',
    );

    it('should pass the environment config through to Keycloak', () => {
      expect(options.config).toEqual({
        url: 'http://localhost:8088',
        realm: 'indigo-eln',
        clientId: 'frontend-client',
      });
    });

    it('should require login on load and redirect back to the app root', () => {
      expect(options.initOptions?.onLoad).toBe('login-required');
      expect(options.initOptions?.redirectUri).toBe('http://localhost/');
    });

    it('should enable auto token refresh', () => {
      expect(options.features?.length).toBe(1);
      expect(SESSION_TIMEOUT_MS).toBe(30 * 60 * 1000);
    });
  });
});
