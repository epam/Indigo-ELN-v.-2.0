import { IsInViewportDirective } from '@/core/directives/is-in-viewport.directive';
import { TwsxPipe } from '@/core/pipes/twsx.pipe';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-infinite-loader',
  templateUrl: './infinite-loader.component.html',
  imports: [IsInViewportDirective, TwsxPipe],
})
export class InfiniteLoaderComponent {
  @Input() classNames?: string;
  @Output() infiniteLoad = new EventEmitter<unknown>();
}
