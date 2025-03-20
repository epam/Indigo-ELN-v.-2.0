import { IEnvironment } from '@/core/types/environment.i';
import { mergeEnvironments } from './env.util';

export const environment: IEnvironment = mergeEnvironments({
  production: true,
  authConfig: {
    authority: '',
    redirectUrl: '',
    clientId: '',
    scope: '',
    responseType: '',
  },
});
