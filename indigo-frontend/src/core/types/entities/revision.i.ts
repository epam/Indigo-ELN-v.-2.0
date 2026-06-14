import { UserRef } from '@core/types/entities/user.i';

export interface RevisionSummary {
  user: UserRef;
  summary: string;
  date: string;
  dateTo?: string;
  revision: number;
  revisionTo?: number;
  details?: RevisionSummary[];
}
