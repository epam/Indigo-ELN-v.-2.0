import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Observable, of } from 'rxjs';
import { SidebarComponent } from './sidebar.component';
import { IdentityService } from '@/core/services/identity.service';
import { ApplicationPermission, CurrentUser } from '@/core/types/entities/user.i';

/** Stubs IdentityService so the sidebar's menu filtering is exercised without HTTP. */
const identityServiceStub = (permissions: ApplicationPermission[]): Partial<IdentityService> => ({
  user$: of({
    id: '00000000-0000-0000-0000-000000000001',
    username: 'admin',
    displayName: 'Administrator',
    permissions,
  }) as Observable<CurrentUser>,
});

describe('SidebarComponent', () => {
  let component: SidebarComponent;
  let fixture: ComponentFixture<SidebarComponent>;

  const createComponent = async (permissions: ApplicationPermission[]) => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, SidebarComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: IdentityService, useValue: identityServiceStub(permissions) },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  };

  describe('with all permissions', () => {
    beforeEach(async () => {
      await createComponent(Object.values(ApplicationPermission));
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
      expect(component.isSidebarOpen).toBe(true);
      expect(component.isHovered).toBe(false);
    });

    it('should show every menu item', () => {
      expect(component.menu().map((item) => item.name)).toEqual([
        'All Projects',
        'Templates',
        'Dictionaries',
        'Signatures',
      ]);
    });

    describe('toggleSidebar', () => {
      it('should toggle isSidebarOpen state', () => {
        expect(component.isSidebarOpen).toBe(true);
        component.toggleSidebar();
        expect(component.isSidebarOpen).toBe(false);
        component.toggleSidebar();
        expect(component.isSidebarOpen).toBe(true);
      });

      it('should reset isHovered to false when toggling', () => {
        component.isHovered = true;
        component.toggleSidebar();
        expect(component.isHovered).toBe(false);
      });
    });

    describe('template rendering', () => {
      it('should render correct number of menu items', () => {
        const menuItems = fixture.nativeElement.querySelectorAll('li');
        // Add 1 to account for the toggle button li
        expect(menuItems.length).toBe(component.menu().length + 1);
      });

      it('should toggle the sidebar width class', () => {
        const sidebar = fixture.nativeElement.querySelector('#sidebar');
        expect(sidebar.classList.contains('w-76')).toBe(true);

        component.toggleSidebar();
        fixture.detectChanges();

        expect(sidebar.classList.contains('w-76')).toBe(false);
        expect(sidebar.classList.contains('w-13.5')).toBe(true);
      });

      it('should toggle sidebar icon class', () => {
        const icon = fixture.nativeElement.querySelector('em');
        expect(icon.classList.contains('indicon-sidebar_close')).toBe(true);
        expect(icon.classList.contains('indicon-sidebar_open')).toBe(false);

        component.toggleSidebar();
        fixture.detectChanges();

        expect(icon.classList.contains('indicon-sidebar_close')).toBe(false);
        expect(icon.classList.contains('indicon-sidebar_open')).toBe(true);
      });
    });
  });

  describe('without permissions', () => {
    beforeEach(async () => {
      await createComponent([]);
    });

    it('should only show unrestricted menu items', () => {
      expect(component.menu().map((item) => item.name)).toEqual(['All Projects']);
    });
  });
});
