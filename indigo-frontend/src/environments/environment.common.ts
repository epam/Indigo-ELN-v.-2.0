import { IEnvironment } from '@/core/types/environment.i';

export const COMMON_ENVIRONMENT: Partial<IEnvironment> = {
  authConfig: {
    secureRoutes: ['/api/eln'],
    scope: 'aws.cognito.signin.user.admin email openid phone profile',
    responseType: 'code',
    useRefreshToken: true,
  },
};
