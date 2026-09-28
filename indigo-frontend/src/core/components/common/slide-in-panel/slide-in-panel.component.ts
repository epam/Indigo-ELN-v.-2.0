import {
  Component,
  ComponentRef,
  computed,
  effect,
  HostListener,
  input,
  output,
  signal,
  TemplateRef,
  Type,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIcon } from '@angular/material/icon';

export interface SlideInPanelConfig {
  inputs?: Record<string, unknown>;
  header?: TemplateRef<any>;
}

@Component({
  selector: 'eln-slide-in-panel',
  standalone: true,
  imports: [CommonModule, MatIcon],
  templateUrl: './slide-in-panel.component.html',
})
export class SlideInPanelComponent {
  topOffset = input<string>('4.5rem');
  width = input<string>('50%');
  header = input<TemplateRef<any> | null>(null);

  afterClose = output<void>();

  @ViewChild('contentOutlet', { read: ViewContainerRef })
  private contentOutlet!: ViewContainerRef;

  private _isOpen = signal(false);
  protected isOpen = this._isOpen.asReadonly();

  // tracks whether the panel has ever been opened, so the slide-out animation plays correctly
  private wasEverOpened = signal(false);
  protected isVisible = computed(() => this._isOpen() || this.wasEverOpened());

  constructor() {
    effect(() => {
      if (this._isOpen()) {
        this.wasEverOpened.set(true);
      }
    });
  }

  open<T>(component: Type<T>, config?: SlideInPanelConfig): ComponentRef<T> {
    this.contentOutlet.clear();
    const ref = this.contentOutlet.createComponent(component);
    if (config?.inputs) {
      Object.entries(config.inputs).forEach(([key, value]) => ref.setInput(key, value));
    }
    this._isOpen.set(true);
    return ref;
  }

  close(): void {
    this._isOpen.set(false);
    this.afterClose.emit();
  }

  @HostListener('document:keydown.escape')
  protected onEscapeKey(): void {
    if (this._isOpen()) {
      this.close();
    }
  }

  protected onBackdropClick(): void {
    this.close();
  }
}
