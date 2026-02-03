import { Component, computed, inject, input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { ProductImageService } from '@core/services/experiment/product-image.service';

@Component({
  selector: 'eln-batch-detail-structure-view',
  templateUrl: './batch-detail-structure-view.component.html',
  styleUrl: './batch-detail-structure-view.component.scss',
  imports: [CommonModule, MatProgressSpinner, MatIconModule, ButtonComponent],
  providers: [ProductImageService],
  standalone: true,
})
export class BatchDetailStructureViewComponent implements OnInit {
  private productImageService = inject(ProductImageService);

  compoundId = input.required<string | null>();
  batchAnchor = input.required<string>();

  imageUrl = computed(() => this.productImageService.imageUrl());
  imageLoading = computed(() => this.productImageService.isLoading());
  imageError = computed(() => this.productImageService.hasError());

  ngOnInit(): void {
    const compoundId = this.compoundId();
    if (compoundId) {
      this.productImageService.load(compoundId);
    } else {
      // No compoundId available - set error state
      console.warn('No compoundId provided for structure view');
      this.productImageService.setError(true);
    }
  }

  onEditStructure() {
    // TODO: Implement edit structure functionality
    console.log('Edit structure clicked for batch:', this.batchAnchor());
  }
}
