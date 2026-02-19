import { Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { ApiImageComponent } from '@core/components/common/image/api-image.component';

@Component({
  selector: 'eln-batch-detail-structure-view',
  templateUrl: './batch-detail-structure-view.component.html',
  styleUrl: './batch-detail-structure-view.component.scss',
  imports: [CommonModule, MatIconModule, ButtonComponent, ApiImageComponent],
  standalone: true,
})
export class BatchDetailStructureViewComponent {
  compoundId = input.required<string | null>();
  batchAnchor = input.required<string>();

  imageUrl = computed(() => {
    const id = this.compoundId();
    return id ? `compounds/${id}/picture` : null;
  });

  onEditStructure() {
    // TODO: Implement edit structure functionality
    console.log('Edit structure clicked for batch:', this.batchAnchor());
  }
}
