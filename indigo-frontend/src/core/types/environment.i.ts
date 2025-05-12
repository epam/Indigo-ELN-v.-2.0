export interface IEnvironment {
  production: boolean;
  authConfig: IAuthConfig;
}

export interface IAuthConfig {
  userPoolId: string;
  userPoolClientId: string;
  identityPoolId: string;
}
