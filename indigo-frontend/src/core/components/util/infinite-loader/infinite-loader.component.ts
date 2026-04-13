import { IsInViewportDirective } from '@/core/directives/is-in-viewport.directive';
import { TwsxPipe } from '@/core/pipes/twsx.pipe';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'eln-infinite-loader',
  templateUrl: './infinite-loader.component.html',
  imports: [IsInViewportDirective, TwsxPipe, MatProgressSpinner],
})
export class InfiniteLoaderComponent {
  @Input() classNames?: string;
  @Input({ required: true }) isLoading: boolean;
  @Output() infiniteLoad = new EventEmitter<unknown>();
}
