import { IEnvironment } from '@/core/types/environment.i';
import { COMMON_ENVIRONMENT } from './environment.common';

export function mergeEnvironments(
  specificEnvironment: IEnvironment,
): IEnvironment {
  return {
    ...COMMON_ENVIRONMENT,
    ...specificEnvironment,
    authConfig: {
      ...COMMON_ENVIRONMENT.authConfig,
      ...specificEnvironment.authConfig,
    },
  };
}
