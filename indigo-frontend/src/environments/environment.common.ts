import { IEnvironment } from '@/core/types/environment.i';

export const COMMON_ENVIRONMENT: Partial<IEnvironment> = {
  authConfig: {
    identityPoolId: '',
    userPoolId: '',
    userPoolClientId: '',
  },
};
