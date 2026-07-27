import { Injectable, Signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { IdentityService } from '@core/services/identity.service';
import { PermissionedEntity } from '@core/types/entities/permission.i';
import { ApplicationPermission } from '@core/types/entities/user.i';
import { map } from 'rxjs';

/**
 * Single source of truth for permission checks in the UI.
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
