import { IdentityService } from '@/core/services/identity.service';
import { CurrentUser, Role } from '@/core/types/entities/user.i';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class RoleGuard implements CanActivate {
  constructor(
    private identityService: IdentityService,
    private router: Router,
  ) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean> {
    const requiredPermission = route.data['requiredPermission'];
    return this.identityService.user$.pipe(
      map((user: CurrentUser) => {
        const hasPermission = user.permissions.some(
          (permission) => permission === requiredPermission,
        );
        if (!hasPermission) {
          this.router.navigate(['/']);
        }
        return hasPermission;
      }),
    );
  }
}
