import { Directive, effect, inject, input, TemplateRef, ViewContainerRef } from '@angular/core';
import { PermissionService } from '@core/services/permission/permission.service';
import { PermissionedEntity } from '@core/types/entities/permission.i';
import { ApplicationPermission } from '@core/types/entities/user.i';

/**
 * Structural directive that shows its content only when the current user has the
 * given permission, hiding it entirely otherwise (mirrors *ngIf ergonomics).
 *
 * Usage:
 * - Global permission check (e.g. creating a new entity):
 *     <button *hasPermission="'CREATE_PROJECTS'">Add Project</button>
 * - Entity-scoped permission check (uses entity.currentPermissions):
 *     <button *hasPermission="'EDIT_PROJECTS'; entity: project()">Edit</button>
 */
@Directive({
  selector: '[hasPermission]',
  standalone: true,
})
export class HasPermissionDirective {
  hasPermission = input.required<ApplicationPermission>();
  hasPermissionEntity = input<PermissionedEntity | null>(null);

  private templateRef = inject(TemplateRef<unknown>);
  private viewContainer = inject(ViewContainerRef);
  private permissionService = inject(PermissionService);

  constructor() {
    effect(() => {
      const allowed = this.permissionService.hasPermission(this.hasPermission(), this.hasPermissionEntity());

      this.viewContainer.clear();
      if (allowed) {
        this.viewContainer.createEmbeddedView(this.templateRef);
      }
    });
  }
}
