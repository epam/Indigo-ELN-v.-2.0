import { ApplicationPermission } from '@core/types/entities/user.i';

export interface PermissionedEntity {
  currentPermissions?: ApplicationPermission[];
}
