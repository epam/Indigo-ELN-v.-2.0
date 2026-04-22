import {
  Component,
  ComponentRef,
  computed,
  effect,
  input,
  output,
  signal,
  Type,
  ViewChild,
  ViewContainerRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';

export interface SlideInPanelConfig {
  inputs?: Record<string, unknown>;
}

@Component({
  selector: 'eln-slide-in-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './slide-in-panel.component.html',
})
export class SlideInPanelComponent {
  topOffset = input<string>('4.5rem');
  width = input<string>('50%');

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

  protected onBackdropClick(): void {
    this.close();
  }
}
