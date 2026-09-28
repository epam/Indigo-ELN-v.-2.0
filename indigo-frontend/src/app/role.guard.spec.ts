import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { roleGuard } from './role.guard';
import { IdentityService } from '@/core/services/identity.service';
import { ApplicationPermission, CurrentUser } from '@/core/types/entities/user.i';

describe('roleGuard', () => {
  const forbiddenUrlTree = {} as UrlTree;

  const runGuard = (permissions: ApplicationPermission[], requiredPermission: ApplicationPermission) => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: IdentityService,
          useValue: {
            user$: of({
              id: '00000000-0000-0000-0000-000000000001',
              username: 'admin',
              displayName: 'Administrator',
              permissions,
            }) as Observable<CurrentUser>,
          },
        },
        { provide: Router, useValue: { parseUrl: () => forbiddenUrlTree } },
      ],
    });

    const route = { data: { requiredPermission } } as unknown as ActivatedRouteSnapshot;

    return TestBed.runInInjectionContext(() => roleGuard(route, {} as RouterStateSnapshot)) as Observable<
      boolean | UrlTree
    >;
  };

  it('should allow the route when the user holds the required permission', (done) => {
    runGuard([ApplicationPermission.MANAGE_DICTIONARIES], ApplicationPermission.MANAGE_DICTIONARIES).subscribe(
      (result) => {
        expect(result).toBe(true);
        done();
      },
    );
  });

  it('should redirect when the user lacks the required permission', (done) => {
    runGuard([ApplicationPermission.VIEW_PROJECTS], ApplicationPermission.MANAGE_DICTIONARIES).subscribe((result) => {
      expect(result).toBe(forbiddenUrlTree);
      done();
    });
  });

  it('should complete so the router can proceed', (done) => {
    runGuard([], ApplicationPermission.MANAGE_TEMPLATES).subscribe({ complete: done });
  });
});
