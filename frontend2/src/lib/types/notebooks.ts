import type {ACLEntry, BaseDTO} from '@/lib/types/common.ts';
import type {ExperimentStatusCounts} from '@/lib/types/experiments.ts';

/** Mirrors BaseNotebookDTO. */
export interface BaseNotebook extends BaseDTO {
  name: string;
  experimentCount: number;
  experimentCountByStatus: ExperimentStatusCounts;
}

/** Mirrors NotebookDTO — the shape the project's notebook list returns. */
export interface Notebook extends BaseNotebook {
  acl: ACLEntry[];
  aclCount: number;
}
