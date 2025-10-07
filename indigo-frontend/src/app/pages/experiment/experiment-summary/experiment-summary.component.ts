import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'eln-experiment-summary',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-6">
      <div class="text-center py-12">
        <div class="text-6xl mb-4">📋</div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">Summary</h3>
        <p class="text-gray-600">Summary functionality coming soon...</p>
      </div>
    </div>
  `,
})
export class ExperimentSummaryComponent { }