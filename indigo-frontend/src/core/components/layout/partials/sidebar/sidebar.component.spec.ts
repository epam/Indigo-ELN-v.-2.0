import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { SidebarComponent } from './sidebar.component';

describe('SidebarComponent', () => {
  let component: SidebarComponent;
  let fixture: ComponentFixture<SidebarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouterTestingModule, SidebarComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.isSidebarOpen).toBe(true);
    expect(component.isHovered).toBe(false);
    expect(component.menu.length).toBe(2);
  });

  it('should have correct menu items', () => {
    const expectedMenu = [
      {
        name: 'Home',
        icon: 'indicon-layers',
        path: '/',
      },
      {
        name: 'Dashboard',
        icon: 'indicon-bell',
        path: '/dashboard',
      },
    ];
    expect(component.menu).toEqual(expectedMenu);
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
      expect(menuItems.length).toBe(component.menu.length + 1);
    });

    it('should toggle sidebar collapsed class', () => {
      const sidebar = fixture.nativeElement.querySelector('#sidebar');
      expect(sidebar.classList.contains('collapsed')).toBe(false);

      component.toggleSidebar();
      fixture.detectChanges();

      expect(sidebar.classList.contains('collapsed')).toBe(true);
    });

    it('should toggle sidebar icon class', () => {
      const icon = fixture.nativeElement.querySelector('em');
      expect(icon.classList.contains('indicon-sidebar_open')).toBe(true);
      expect(icon.classList.contains('indicon-sidebar_close')).toBe(false);

      component.toggleSidebar();
      fixture.detectChanges();

      expect(icon.classList.contains('indicon-sidebar_open')).toBe(false);
      expect(icon.classList.contains('indicon-sidebar_close')).toBe(true);
    });
  });
});
