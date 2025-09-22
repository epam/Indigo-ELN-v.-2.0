import { AvatarComponent } from '@/core/components/common/avatar/avatar.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { Experiment } from '@/core/types/entities/experiment.i';
import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';

@Component({
  selector: 'eln-experiment-card',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    AvatarComponent
  ],
  templateUrl: './experiment-card.component.html',
  styleUrls: ['./experiment-card.component.scss']
})
export class ExperimentCardComponent {
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
  @Input() experiment: Experiment;
  @Input() variant: 'grid' | 'list' = 'grid';
  isFavorite: boolean = false;
  toggleFavorite(event: MouseEvent): void {
    event.stopPropagation(); // Prevent card click
    this.isFavorite = !this.isFavorite;
  }

  getStatusClass(status: ExperimentStatus): string {
    const statusMap: { [key: string]: string } = {
      [ExperimentStatus.OPEN]: 'status-open',
      [ExperimentStatus.WAITING_FOR_SIGNATURE]: 'status-waiting-for-signature',
      [ExperimentStatus.COMPLETED]: 'status-completed',
      [ExperimentStatus.REJECTED]: 'status-rejected'
    };
    return statusMap[status];
  }

}
