import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DOCUMENT } from '@angular/common';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class DownloadService {
  private httpClient = inject(HttpClient);
  private document = inject(DOCUMENT);

  downloadPost(url: string, fallbackFilename = 'download'): Observable<void> {
    return new Observable<void>((observer) => {
      this.httpClient
        .post(url, null, {
          responseType: 'blob',
          observe: 'response',
        })
        .subscribe({
          next: (response) => {
            const filename = this.extractFilename(response.headers.get('Content-Disposition'), fallbackFilename);
            this.triggerDownload(response.body, filename);
            observer.next();
            observer.complete();
          },
          error: (err) => observer.error(err),
        });
    });
  }

  private extractFilename(contentDisposition: string | null, fallback: string): string {
    if (!contentDisposition) return fallback;
    // Try filename*=UTF-8''... first (RFC 5987)
    const rfcMatch = contentDisposition.match(/filename\*=(?:UTF-8'')?([^;]+)/i);
    if (rfcMatch) return decodeURIComponent(rfcMatch[1].trim().replace(/^["']|["']$/g, ''));
    // Fall back to plain filename="..."
    const plainMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
    if (plainMatch) return plainMatch[1].trim();
    return fallback;
  }

  private triggerDownload(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const anchor = this.document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.style.display = 'none';
    this.document.body.appendChild(anchor);
    anchor.click();
    this.document.body.removeChild(anchor);
    URL.revokeObjectURL(url);
  }
}
