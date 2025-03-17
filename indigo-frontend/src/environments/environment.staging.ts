import { IEnvironment } from '@/core/types/environment.i';

export const environment: IEnvironment = {
  production: true,
  authConfig: {
    authority: '',
    redirectUrl: '',
    clientId: '',
    scope: '',
    responseType: '',
  },
};
