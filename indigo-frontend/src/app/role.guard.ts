import { IdentityService } from '@/core/services/identity.service';
import { ApplicationPermission } from '@/core/types/entities/user.i';
import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { map, Observable, take } from 'rxjs';

export const roleGuard: CanActivateFn = (route): Observable<boolean | UrlTree> => {
  const identityService = inject(IdentityService);
  const router = inject(Router);
  const requiredPermission = route.data['requiredPermission'] as ApplicationPermission;

  return identityService.user$.pipe(
    take(1),
    map((user) => user.permissions.includes(requiredPermission) || router.parseUrl('/')),
  );
};
