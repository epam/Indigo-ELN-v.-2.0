import { ExperimentStatus } from '@/core/enums/experiment-status.enum';

// Shared types for experiment status decoration
export type BadgeVariant = 'blue' | 'green' | 'yellow' | 'red' | 'grey' | 'violet';

export interface ExperimentStatusDecoration {
  variant: BadgeVariant;
  dotClass: string;
}

/**
 * Centralized mapping for experiment status decorations
 * Used across different components to ensure consistent styling
 */
export const EXPERIMENT_STATUS_DECORATION_MAP: Record<ExperimentStatus, ExperimentStatusDecoration> = {
  [ExperimentStatus.OPEN]: { variant: 'blue', dotClass: 'bg-primary-400' },
  [ExperimentStatus.REOPEN]: { variant: 'blue', dotClass: 'bg-primary-400' },
  [ExperimentStatus.COMPLETED]: { variant: 'green', dotClass: 'bg-green-200' },
  [ExperimentStatus.SUBMITTED]: {
    variant: 'yellow',
    dotClass: 'bg-yellow-200',
  },
  [ExperimentStatus.SIGNING]: { variant: 'yellow', dotClass: 'bg-yellow-200' },
  [ExperimentStatus.REJECTED]: { variant: 'red', dotClass: 'bg-red-200' },
  [ExperimentStatus.SIGNED]: { variant: 'green', dotClass: 'bg-green-200' },
  [ExperimentStatus.ARCHIVED]: { variant: 'grey', dotClass: 'bg-neutral-500' },
  [ExperimentStatus.CANCELLED]: { variant: 'red', dotClass: 'bg-red-200' },
};

/**
 * Get the badge variant for a given experiment status
 * @param status - The experiment status
 * @returns The badge variant (color)
 */
export function getExperimentStatusBadgeVariant(status: ExperimentStatus): BadgeVariant {
  return EXPERIMENT_STATUS_DECORATION_MAP[status]?.variant || 'grey';
}
