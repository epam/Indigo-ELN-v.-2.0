import type { ACLEntry, BaseDTO } from '@/lib/types/common.ts';
import type { ExperimentStatusCounts } from '@/lib/types/experiments.ts';

export interface BaseProject extends BaseDTO {
  name: string;
  notebookCount: number;
  experimentCount: number;
  experimentCountByStatus: ExperimentStatusCounts;
}

export interface Project extends BaseProject {
  acl: ACLEntry[];
  aclCount: number;
}

export type SortOrder = 'EARLIEST' | 'LATEST';

export interface ProjectFilters {
  search: string;
  sort: SortOrder;
  createdByMe: boolean;
}

export interface TotalCounts {
  projects: number;
  notebooks: number;
  experiments: number;
  experimentsByStatus: ExperimentStatusCounts;
}
