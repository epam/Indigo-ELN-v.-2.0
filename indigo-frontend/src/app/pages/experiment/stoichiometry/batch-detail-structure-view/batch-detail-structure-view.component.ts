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

  experimentId = input.required<string>();
  batchAnchor = input.required<string>();
  outputAnchor = input.required<string>();

  imageUrl = computed(() => this.productImageService.imageUrl());
  imageLoading = computed(() => this.productImageService.isLoading());
  imageError = computed(() => this.productImageService.hasError());

  ngOnInit(): void {
    const experimentId = this.experimentId();
    const outputAnchor = this.outputAnchor();
    if (experimentId && outputAnchor) {
      this.productImageService.load(experimentId, outputAnchor);
    }
  }

  onEditStructure() {
    // TODO: Implement edit structure functionality
    console.log('Edit structure clicked for batch:', this.batchAnchor());
  }
}
