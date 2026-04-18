export interface IEnvironment {
  production: boolean;
  authConfig: IAuthConfig;
  authProvider: string;
  keycloak?: IKeycloakConfig;
}

export interface IAuthConfig {
  userPoolId: string;
  userPoolClientId: string;
  identityPoolId: string;
}

export interface IKeycloakConfig {
  url: string;
  realm: string;
  clientId: string;
}
