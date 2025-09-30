import { AvatarComponent } from '@/core/components/common/avatar/avatar.component';
import { BadgeComponent } from '@/core/components/common/badge/badge.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { Experiment } from '@/core/types/entities/experiment.i';
import { CommonModule } from '@angular/common';
import { Component, Input, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { NormalizeLabelPipe } from '@/core/pipes/normalizeLabe.pipe';
import { getExperimentStatusBadgeVariant } from '@/core/utils/experiment-status.util';

@Component({
  selector: 'eln-experiment-item',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    AvatarComponent,
    BadgeComponent,
    NormalizeLabelPipe,
  ],
  templateUrl: './experiment-item.component.html',
})
export class ExperimentItemComponent {
  private router = inject(Router);
  
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

  @Input() experiment!: Experiment;
  @Input() variant: 'grid' | 'list' = 'grid';
  @Input() projectId!: string;
  @Input() notebookId!: string;

  openDetails(): void {
    if (!this.experiment || !this.projectId || !this.notebookId) return;
    
    const url = `/projects/${this.projectId}/notebooks/${this.notebookId}/experiments/${this.experiment.id}`;
    this.router.navigateByUrl(url);
  }

  getStatusBadgeVariant = getExperimentStatusBadgeVariant;

  get statusLabel(): string {
    return this.experiment.status;
  }
}