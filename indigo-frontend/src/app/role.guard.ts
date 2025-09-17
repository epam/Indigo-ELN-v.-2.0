import { UserService } from '@/core/services/user.service';
import { Role } from '@/core/types/entities/user.i';
import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class RoleGuard implements CanActivate {
  constructor(private userService: UserService, private router: Router) {}

  canActivate(route: import('@angular/router').ActivatedRouteSnapshot): Observable<boolean> {
    const requiredRole = route.data['requiredRole'];
    return this.userService.userRoles$.pipe(
      map((roles: Role[]) => {
        const hasRole = roles.some((role) => role.name === requiredRole);
        if (!hasRole) {
          this.router.navigate(['/']);
        }
        return hasRole;
      })
    );
  }
}