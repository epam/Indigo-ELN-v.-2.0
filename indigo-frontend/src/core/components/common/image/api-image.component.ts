import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnChanges, SimpleChanges } from '@angular/core';
import { IsInViewportDirective } from '@core/directives/is-in-viewport.directive';
import { ApiService } from '@core/services/api.service';

@Component({
  selector: 'eln-api-image',
  templateUrl: './api-image.component.html',
  standalone: true,
  imports: [CommonModule, IsInViewportDirective],
})
export class ApiImageComponent implements OnChanges {
  @Input() url: string;
  @Input() blob: string;
  @Input() altText = '';
  api = inject(ApiService);

  src: string | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['url'] || changes['blob']) {
      this.src = null;
    }
  }

  load() {
    if (this.blob) {
      this.src = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(this.blob)}`;
    } else if (this.url) {
      this.api
        .request<Blob>('get', this.url, null, { responseType: 'blob' })
        .subscribe((blob) => (this.src = URL.createObjectURL(blob)));
    }
  }
}
