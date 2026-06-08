import { UserRef } from '@core/types/entities/user.i';

export interface Revision {
  revision: number;
  datetime: string;
  user: UserRef;
  displayName: string;
  summary: string;
  diff: unknown;
  stringDiff: string;
}
