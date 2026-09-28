import { Component, computed, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TeamComponent } from '@core/components/common/team/team.component';
import { TeamComponentConfig } from '@core/components/common/team/team.config';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { ACLEntry } from '@core/types/entities/acl.i';

@Component({
  selector: 'eln-experiment-team-drawer',
  standalone: true,
  imports: [CommonModule, TeamComponent],
  templateUrl: './experiment-team-drawer.component.html',
})
export class ExperimentTeamDrawerComponent {
  experimentId = input.required<string>();

  private experimentDetailService = inject(ExperimentDetailService);

  experiment = computed(() => this.experimentDetailService.experimentDetail());

  teamConfig: TeamComponentConfig = {
    buildAccessEndpoint: (id: string) => `experiments/${id}/access`,
  };

  onTeamChanged(updatedTeam: ACLEntry[]): void {
    const currentExperiment = this.experimentDetailService.experimentDetail();
    if (currentExperiment) {
      this.experimentDetailService.experimentDetail.set({
        ...currentExperiment,
        acl: updatedTeam,
      });
    }
  }
}
