import { CommonModule } from '@angular/common';
import { Component, DestroyRef, inject, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { RouterLink } from '@angular/router';
import { AvatarComponent } from '../../common/avatar/avatar.component';
import { CardComponent } from '../../common/card/card.component';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { ApiService } from '@core/services/api.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { downloadBlob } from '@core/utils/download.util';
import { ExperimentForSignature } from '@core/types/entities/experiments/experiment-detail.i';

@Component({
  selector: 'eln-signature-item',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    AvatarComponent,
    RouterLink,
  ],
  templateUrl: './signature-item.component.html',
})
export class SignatureItemComponent {
  mock_users = [
    'assets/avatar1.png',
    'assets/avatar2.png',
    'assets/avatar3.png',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
    '-',
  ];
  @Input() experiment: ExperimentForSignature;
  @Input() currentUser: string | null;
  @Input() variant: 'grid' | 'list' = 'grid';

  service = inject(ApiService);
  destroyRef = inject(DestroyRef);

  download() {
    this.service
      .request<Blob>(
        'get',
        `/signature/experiments/${this.experiment.id}/download`,
        undefined,
        {
          responseType: 'blob',
        },
      )
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        catchError((err) => {
          console.error('Failed to download attachment:', err);
          return of(null);
        }),
      )
      .subscribe({
        // TODO read filename from Content-Disposition header
        next: (blob: Blob | null) =>
          downloadBlob(blob, `Experiment ${this.experiment.name}.pdf`),
      });
  }

  approve() {
    this.service
      .request<ExperimentForSignature>(
        'post',
        `experiments/${this.experiment.id}/workflow/approve`,
      )
      .pipe(
        catchError((err) => {
          console.error('Failed to approve experiment:', err);
          // TODO show error in UI
          return of(null);
        }),
      )
      .subscribe({
        next: (updated) => {
          if (this.experiment && updated) this.experiment = updated;
        },
      });
  }

  reject() {
    this.service
      .request<ExperimentForSignature>(
        'post',
        `experiments/${this.experiment.id}/workflow/reject`,
      )
      .pipe(
        catchError((err) => {
          console.error('Failed to reject experiment:', err);
          // TODO show error in UI
          return of(null);
        }),
      )
      .subscribe({
        next: (updated) => {
          if (this.experiment && updated) this.experiment = updated;
        },
      });
  }
}
