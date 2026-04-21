import {
  ApplicationRef,
  ComponentRef,
  createComponent,
  EnvironmentInjector,
  inject,
  Injectable,
  Type,
} from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { SlideInPanelComponent, SlideInPanelConfig } from './slide-in-panel.component';

const SLIDE_OUT_ANIMATION_MS = 300;

export class SlideInPanelRef<T> {
  readonly instance: T;
  readonly afterClosed: Observable<void>;

  constructor(
    instance: T,
    private readonly _close: () => void,
    afterClosed: Observable<void>,
  ) {
    this.instance = instance;
    this.afterClosed = afterClosed;
  }

  close(): void {
    this._close();
  }
}

@Injectable({ providedIn: 'root' })
export class SlideInPanelService {
  private appRef = inject(ApplicationRef);
  private environmentInjector = inject(EnvironmentInjector);

  private currentPanel: ComponentRef<SlideInPanelComponent> | null = null;

  open<T>(component: Type<T>, config?: SlideInPanelConfig): SlideInPanelRef<T> {
    this.close();

    const panelRef = createComponent(SlideInPanelComponent, {
      environmentInjector: this.environmentInjector,
    });
    this.appRef.attachView(panelRef.hostView);
    document.body.appendChild(panelRef.location.nativeElement);
    this.currentPanel = panelRef;

    // Run initial change detection so @ViewChild (contentOutlet) is resolved
    panelRef.changeDetectorRef.detectChanges();

    const contentRef: ComponentRef<T> = panelRef.instance.open(component, config);

    const afterClosed$ = new Subject<void>();
    panelRef.instance.afterClose.subscribe(() => {
      afterClosed$.next();
      afterClosed$.complete();
      // Wait for slide-out animation before destroying
      setTimeout(() => this.destroy(panelRef), SLIDE_OUT_ANIMATION_MS);
      this.currentPanel = null;
    });

    return new SlideInPanelRef<T>(contentRef.instance, () => panelRef.instance.close(), afterClosed$.asObservable());
  }

  close(): void {
    this.currentPanel?.instance.close();
  }

  private destroy(panelRef: ComponentRef<SlideInPanelComponent>): void {
    this.appRef.detachView(panelRef.hostView);
    panelRef.location.nativeElement.remove();
    panelRef.destroy();
  }
}
