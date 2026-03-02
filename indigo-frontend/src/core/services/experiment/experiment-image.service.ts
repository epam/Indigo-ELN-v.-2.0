import { inject, Injectable, signal } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import { finalize } from 'rxjs';

@Injectable()
export class ExperimentImageService {
  private service = inject(ApiService);

  // Signals for image state
  readonly imageUrl = signal<string | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);
  private readonly currentId = signal<string | null>(null);

  // Load experiment picture
  load(experimentId: string) {
    if (this.currentId() === experimentId) {
      return;
    }
    this.currentId.set(experimentId);
    this.isLoading.set(true);
    this.hasError.set(false);

    this.service
      .request<Blob>('get', `experiments/${experimentId}/picture`, undefined, {
        responseType: 'blob',
      })
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (blob) => {
          const currentUrl = this.imageUrl();
          if (currentUrl) {
            URL.revokeObjectURL(currentUrl);
          }
          this.imageUrl.set(URL.createObjectURL(blob));
        },
        error: () => this.hasError.set(true),
      });
  }

  // Utility methods
  refresh() {
    const id = this.currentId();
    if (id) this.load(id);
  }

  reset() {
    // Limpiar URL antes de resetear
    const currentUrl = this.imageUrl();
    if (currentUrl) {
      URL.revokeObjectURL(currentUrl);
    }

    this.currentId.set(null);
    this.imageUrl.set(null);
    this.isLoading.set(false);
    this.hasError.set(false);
  }
}
