import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DOCUMENT } from '@angular/common';
import { Observable } from 'rxjs';
import { parse } from 'content-disposition';

@Injectable({
  providedIn: 'root',
})
export class DownloadService {
  private httpClient = inject(HttpClient);
  private document = inject(DOCUMENT);

  download(method: 'get' | 'post', url: string, fallbackFilename = 'download'): Observable<void> {
    return new Observable<void>((observer) => {
      this.httpClient
        .request(method, url, {
          responseType: 'blob',
          observe: 'response',
        })
        .subscribe({
          next: (response) => {
            const contentDisposition = response.headers.get('Content-Disposition');
            const filename =
              (contentDisposition != null ? parse(contentDisposition)?.parameters?.['filename'] : null) ||
              fallbackFilename;
            this.triggerDownload(response.body, filename);
            observer.next();
            observer.complete();
          },
          error: (err) => observer.error(err),
        });
    });
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
