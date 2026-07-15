import { Injectable, Signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { IdentityService } from '@core/services/identity.service';
import { PermissionedEntity } from '@core/types/entities/permission.i';
import { ApplicationPermission } from '@core/types/entities/user.i';
import { map } from 'rxjs';

/**
 * Single source of truth for permission checks in the UI.
 *
 * Rule of thumb (confirmed across Project and Notebook permission tickets):
 * - Actions that create a new entity (no instance exists yet) are gated by the
 *   current user's GLOBAL permissions (`CurrentUser.permissions`).
 * - Actions on an existing entity (edit, attachments, manage access) are gated by
 *   that entity's `currentPermissions` (already merged by the backend: global + the
 *   user's access level on that specific instance).
 *
 * Defaults to `false` (hidden) whenever data isn't available yet (user not loaded,
 * entity not loaded, or entity has no currentPermissions) to avoid flashing UI the
 * user isn't allowed to use.
 */
@Injectable({
  providedIn: 'root',
})
export class PermissionService {
  private readonly currentUserPermissions: Signal<ApplicationPermission[] | undefined>;

  constructor(private identityService: IdentityService) {
    this.currentUserPermissions = toSignal(this.identityService.user$.pipe(map((user) => user.permissions)));
  }

  hasPermission(permission: ApplicationPermission, entity?: PermissionedEntity | null): boolean {
    const permissions = entity ? entity.currentPermissions : this.currentUserPermissions();
    return !!permissions?.includes(permission);
  }
}
