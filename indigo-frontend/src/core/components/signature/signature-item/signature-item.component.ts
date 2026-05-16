import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { CardComponent } from '../../common/card/card.component';
import { ApiService } from '@core/services/api.service';
import { DownloadService } from '@core/services/download.service';
import { Document } from '@core/types/entities/document.i';

@Component({
  selector: 'eln-signature-item',
  standalone: true,
  imports: [CommonModule, CardComponent, MatButtonModule, MatIconModule, MatMenuModule],
  templateUrl: './signature-item.component.html',
})
export class SignatureItemComponent {
  @Input() document: Document;
  @Input() variant: 'grid' | 'list' = 'grid';

  service = inject(ApiService);
  downloadService = inject(DownloadService);
  destroyRef = inject(DestroyRef);

  download() {
    this.downloadService.download('get', `/api/signature/documents/${this.document.id}/download`).subscribe();
  }

  approve() {
    this.service.request<Document>('post', `/api/signature/documents/${this.document.id}/sign`).subscribe({
      next: (updated) => {
        this.document = updated;
      },
    });
  }

  reject() {
    this.service.request<Document>('post', `/api/signature/documents/${this.document.id}/reject`).subscribe({
      next: (updated) => {
        this.document = updated;
      },
    });
  }
}
