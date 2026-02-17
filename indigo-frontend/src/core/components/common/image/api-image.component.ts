import { CommonModule } from '@angular/common';
import { Component, inject, Input } from '@angular/core';
import { IsInViewportDirective } from '@core/directives/is-in-viewport.directive';
import { ApiService } from '@core/services/api.service';

@Component({
  selector: 'eln-api-image',
  templateUrl: './api-image.component.html',
  standalone: true,
  imports: [CommonModule, IsInViewportDirective],
})
export class ApiImageComponent {
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
      });
  }
}
