import { inject, Injectable, signal } from '@angular/core';
import { ApiService } from '@/core/services/api.service';

@Injectable()
export class ProductImageService {
  private service = inject(ApiService);

  // Signals for image state
  readonly imageUrl = signal<string | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);

  // Actual endpoint: GET /api/eln/compounds/{compoundID}/picture
  // Returns JSON with image data (likely SVG string or base64)
  load(compoundId: string) {
    console.log(`ProductImageService.load(compoundId: ${compoundId})`);
    this.setLoading(true);
    this.setError(false);

    this.service
      .request<any>('get', `compounds/${compoundId}/picture`)
      .subscribe({
        next: (response) => {
          // Response is JSON - could be SVG string, base64, or image URL
          if (typeof response === 'string') {
            // Direct SVG string or URL
            this.setImageUrl(response);
          } else if (response?.svg) {
            // SVG in a property
            const blob = new Blob([response.svg], { type: 'image/svg+xml' });
            const objectUrl = URL.createObjectURL(blob);
            this.setImageUrl(objectUrl);
          } else if (response?.data) {
            // Base64 or other data format
            this.setImageUrl(response.data);
          } else {
            // Unknown format
            console.warn('Unknown response format:', response);
            this.setError(true);
          }
          this.setLoading(false);
        },
        error: (error) => {
          console.error(`Error loading compound structure image:`, error);
          this.setImageUrl(null);
          this.setError(true);
          this.setLoading(false);
        },
      });
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
  refresh(compoundId: string) {
    this.load(compoundId);
  }

  reset() {
    this.setImageUrl(null);
    this.setLoading(false);
    this.setError(false);
  }
}
