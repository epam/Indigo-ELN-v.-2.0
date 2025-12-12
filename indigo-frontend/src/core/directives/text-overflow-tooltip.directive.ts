import {
  Directive,
  ElementRef,
  Input,
  OnInit,
  OnDestroy,
  HostListener,
} from '@angular/core';
import { MatTooltip } from '@angular/material/tooltip';
import { Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';

/**
 * Directive that automatically shows a tooltip when text overflows
 * Usage: <p appTextOverflowTooltip="tooltip text">Some long text here</p>
 * or with dynamic content: <p [appTextOverflowTooltip]="dynamicText">{{ dynamicText }}</p>
 */
@Directive({
  selector: '[appTextOverflowTooltip]',
  standalone: true,
  hostDirectives: [MatTooltip],
})
export class TextOverflowTooltipDirective implements OnInit, OnDestroy {
  @Input() set appTextOverflowTooltip(text: string) {
    this.tooltipText = text;
    this.updateTooltip();
  }

  private tooltipText = '';
  private destroy$ = new Subject<void>();
  private resizeSubject$ = new Subject<void>();

  constructor(
    private elementRef: ElementRef,
    private matTooltip: MatTooltip,
  ) {
    this.matTooltip.tooltipClass = 'tooltip-shadow';
    this.matTooltip.position = 'above';

    // Debounce resize checks
    this.resizeSubject$
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe(() => this.updateTooltip());
  }

  ngOnInit(): void {
    this.updateTooltip();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('window:resize')
  onWindowResize(): void {
    this.resizeSubject$.next();
  }

  private updateTooltip(): void {
    // Use setTimeout to ensure DOM is updated
    setTimeout(() => {
      if (this.isTextOverflowing()) {
        this.matTooltip.message = this.tooltipText;
        this.matTooltip.disabled = false;
      } else {
        this.matTooltip.disabled = true;
      }
    }, 0);
  }

  private isTextOverflowing(): boolean {
    const element = this.elementRef.nativeElement;
    return (
      element.scrollWidth > element.clientWidth ||
      element.scrollHeight > element.clientHeight
    );
  }
}
