import { inject, Injectable, signal } from '@angular/core';
import { ApiService } from '@/core/services/api.service';

@Injectable()
export class ProductImageService {
  private service = inject(ApiService);

  // Signals for image state
  readonly imageUrl = signal<string | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);

  // TODO: Replace with actual endpoint once available
  // Expected endpoint: GET /experiments/{experimentId}/outputs/{outputAnchor}/structure-image
  load(experimentId: string, outputAnchor: string) {
    console.log(`ProductImageService.load(${experimentId}, ${outputAnchor})`);
    this.setLoading(true);
    this.setError(false);

    // Using placeholder image for now
    // TODO: Replace with actual API call when endpoint is available
    setTimeout(() => {
      // Placeholder: Using a generic chemical structure placeholder
      this.setImageUrl('https://via.placeholder.com/300x300/f0f0f0/666666?text=Chemical+Structure');
      this.setLoading(false);
    }, 500);

    /* Future implementation:
    this.service
      .request<Blob>('get', `experiments/${experimentId}/outputs/${outputAnchor}/structure-image`, {
        responseType: 'blob'
      })
      .subscribe({
        next: (blob) => {
          const objectUrl = URL.createObjectURL(blob);
          this.setImageUrl(objectUrl);
          this.setLoading(false);
        },
        error: (error) => {
          console.warn(`Error loading product structure image:`, error);
          this.setImageUrl(null);
          this.setError(true);
          this.setLoading(false);
        },
      });
    */
  }

  // Setter methods
  setImageUrl(url: string | null) {
    this.imageUrl.set(url);
  }

  setLoading(value: boolean) {
    this.isLoading.set(value);
  }

  setError(value: boolean) {
    this.hasError.set(value);
  }

  // Utility methods
  refresh(experimentId: string, outputAnchor: string) {
    this.load(experimentId, outputAnchor);
  }

  reset() {
    this.setImageUrl(null);
    this.setLoading(false);
    this.setError(false);
  }
}
