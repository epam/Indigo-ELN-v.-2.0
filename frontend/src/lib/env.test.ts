import { describe, expect, it } from 'vitest';

import { parseCognitoConfig, parseKeycloakConfig } from '@/lib/env';

const COGNITO = { cognitoUserPoolId: 'us-east-1_pool', cognitoClientId: 'client-id' };
const KEYCLOAK_ENV = {
  VITE_KEYCLOAK_URL: 'http://localhost:8088',
  VITE_KEYCLOAK_REALM: 'indigo-eln',
  VITE_KEYCLOAK_CLIENT_ID: 'frontend-client',
};

describe('parseCognitoConfig', () => {
  it('defaults the provider, so a config.json predating this option still loads', () => {
    expect(parseCognitoConfig(COGNITO)).toEqual({ authProvider: 'cognito', ...COGNITO });
  });

  it('rejects a missing pool id', () => {
    expect(() => parseCognitoConfig({ cognitoClientId: 'client-id' })).toThrow();
  });

  it('rejects a body that is not an object', () => {
    expect(() => parseCognitoConfig('nope')).toThrow();
  });
});

describe('parseKeycloakConfig', () => {
  it('reads the three build-time vars', () => {
    expect(parseKeycloakConfig(KEYCLOAK_ENV)).toEqual({
      authProvider: 'keycloak',
      keycloakUrl: 'http://localhost:8088',
      keycloakRealm: 'indigo-eln',
      keycloakClientId: 'frontend-client',
    });
  });

  it('names the missing variable, so a bad build arg is obvious', () => {
    expect(() =>
      parseKeycloakConfig({
        VITE_KEYCLOAK_URL: 'http://localhost:8088',
        VITE_KEYCLOAK_CLIENT_ID: 'frontend-client',
      }),
    ).toThrow(/VITE_KEYCLOAK_REALM/);
  });

  it('ignores Cognito settings rather than demanding them', () => {
    expect(parseKeycloakConfig({ ...KEYCLOAK_ENV, ...COGNITO })).toMatchObject({ authProvider: 'keycloak' });
  });
});
