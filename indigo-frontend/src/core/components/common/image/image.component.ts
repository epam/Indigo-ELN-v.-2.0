import { CommonModule } from '@angular/common';
import { Component, forwardRef, inject, Input } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { IsInViewportDirective } from '@core/directives/is-in-viewport.directive';
import { ApiService } from '@core/services/api.service';

@Component({
  selector: 'eln-image',
  templateUrl: './image.component.html',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ImageComponent),
      multi: true,
    },
  ],
  imports: [CommonModule, IsInViewportDirective],
})
export class ImageComponent {
  @Input({ required: true }) url: string;
  @Input() altText = '';
  api = inject(ApiService);

  src: string | null = null;

  load() {
    this.api
      .request<Blob>('get', this.url, null, { responseType: 'blob' })
      .subscribe({
        next: (blob) => {
          this.src = URL.createObjectURL(blob);
        },
        error: (error) => {
          console.error('Error loading image:', error);
        },
      });
  }
}
