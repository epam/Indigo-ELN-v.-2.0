import { ApplicationPermission } from '@core/types/entities/user.i';

/**
 * Implemented by entities that carry the current user's computed permissions
 * for that specific instance (global permissions merged with the user's
 * access level on that entity), e.g. Project, and in the future Notebook/Experiment.
 *
 * Optional because list endpoints (e.g. GET /projects) don't return this field today,
 * only single-entity detail endpoints (e.g. GET /projects/{id}) do.
 */
export interface PermissionedEntity {
  currentPermissions?: ApplicationPermission[];
}
