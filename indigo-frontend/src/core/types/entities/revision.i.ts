import { UserMetadata } from '@core/types/entities/user.i';

export interface Revision {
  revision: number;
  datetime: string;
  user: UserMetadata;
  displayName: string;
  summary: string;
  diff: unknown;
  stringDiff: string;
}
