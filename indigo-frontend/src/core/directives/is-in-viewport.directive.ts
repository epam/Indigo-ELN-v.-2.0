import { isPlatformBrowser } from '@angular/common';
import {
  Directive,
  ElementRef,
  EventEmitter,
  Inject,
  Input,
  OnDestroy,
  OnInit,
  Output,
  PLATFORM_ID,
} from '@angular/core';

/**
 * Directive that detects when an element enters or exits the viewport
 * Usage: <div [appIsInViewport]="true" (enteredViewport)="onEnter()" (exitedViewport)="onExit()"></div>
 */
@Directive({
  selector: '[appIsInViewport]',
  standalone: true,
})
export class IsInViewportDirective implements OnInit, OnDestroy {
  private observer?: IntersectionObserver;
  private previouslyInView = false;

  @Input() appIsInViewport = true;

  @Input() triggerOnce = false;
  @Input() rootMargin = '0px';
  @Input() threshold = 0;
  @Input() rootElement?: Element;

  @Input() set watch(value: unknown) {
    if (!value || !this.appIsInViewport) return;

    this.initObserver();
  }

  // Output events
  @Output() enteredViewport = new EventEmitter<IntersectionObserverEntry>();
  @Output() exitedViewport = new EventEmitter<IntersectionObserverEntry>();
  @Output() visibilityChange = new EventEmitter<{
    isVisible: boolean;
    entry: IntersectionObserverEntry;
  }>();

  constructor(
    private elementRef: ElementRef,
    @Inject(PLATFORM_ID) private platformId: object,
  ) {}

  ngOnInit(): void {
    if (!this.appIsInViewport || !isPlatformBrowser(this.platformId)) return;

    this.initObserver();
  }

  ngOnDestroy(): void {
    this.cleanupObserver();
  }

  private initObserver(): void {
    this.cleanupObserver();

    if (!isPlatformBrowser(this.platformId)) return;

    this.observer = new IntersectionObserver(
      (entries) => this.handleIntersection(entries),
      {
        threshold: this.threshold,
        rootMargin: this.rootMargin,
        root: this.rootElement || null,
      },
    );

    this.observer.observe(this.elementRef.nativeElement);
  }

  private handleIntersection(entries: IntersectionObserverEntry[]): void {
    const entry = entries[0];

    if (!entry) return;

    const isInView = entry.isIntersecting;
    const element = this.elementRef.nativeElement;

    if (isInView) {
      element.classList.add('in-viewport');
    } else {
      element.classList.remove('in-viewport');
    }

    if (isInView && !this.previouslyInView) {
      this.enteredViewport.emit(entry);

      if (this.triggerOnce) {
        this.cleanupObserver();
      }
    } else if (!isInView && this.previouslyInView) {
      this.exitedViewport.emit(entry);
    }

    this.visibilityChange.emit({
      isVisible: isInView,
      entry,
    });

    this.previouslyInView = isInView;
  }

  private cleanupObserver(): void {
    if (this.observer) {
      this.observer.disconnect();
      this.observer = undefined;
    }
  }
}
