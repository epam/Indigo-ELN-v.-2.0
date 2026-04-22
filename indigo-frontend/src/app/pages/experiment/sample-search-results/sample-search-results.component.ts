import { CommonModule } from '@angular/common';
import { Component, DestroyRef, EventEmitter, inject, Input, Output } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { Sample } from '@core/types/entities/experiments/search.i';
import {
  ColumnDefDirective,
  ExpandableTableComponent,
} from '@core/components/common/expandable-table/expandable-table.component';
import { ApiImageComponent } from '@core/components/common/image/api-image.component';
import { InfiniteLoaderComponent } from '@core/components/util/infinite-loader/infinite-loader.component';
import { SamplesSearchLoader } from '@core/components/util/infinite-scroll-search';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon, MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '@core/services/api.service';

@Component({
  standalone: true,
  selector: 'eln-sample-search-results',
  imports: [
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    ExpandableTableComponent,
    ColumnDefDirective,
    ApiImageComponent,
    InfiniteLoaderComponent,
    MatTooltip,
    MatIcon,
    MatFormFieldModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './sample-search-results.component.html',
})
export class SampleSearchResultsComponent {
  @Input({ required: true }) loader: SamplesSearchLoader;
  @Output() addToExperiment = new EventEmitter<Sample>();

  destroyRef = inject(DestroyRef);
  dialog = inject(MatDialog);
  apiService = inject(ApiService);

  markSample(sample: Sample, mark: boolean) {
    this.apiService
      .request<Sample>('post', `samples/${sample.id}/${mark ? 'mark' : 'unmark'}`)
      .subscribe((response) => {
        this.loader.replace((s) => s.id === sample.id, response);
      });
  }

  protected readonly encodeURIComponent = encodeURIComponent;
}
