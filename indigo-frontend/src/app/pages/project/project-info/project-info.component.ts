import { AttachmentComponent } from '@/core/components/common/attachment/attachment.component';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ChipComponent } from '@/core/components/common/chip/chip.component';
import { TeamComponent } from '@/core/components/project/team/team.component';
import { ApiService } from '@/core/services/api.service';
import { Project } from '@/core/types/entities/project.i';
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'eln-project-info',
  standalone: true,
  imports: [
    CommonModule,
    ButtonComponent,
    ChipComponent,
    AttachmentComponent,
    TeamComponent,
    CardComponent,
  ],
  templateUrl: './project-info.component.html',
})
export class ProjectInfoComponent implements OnInit {
  activedRoute = inject(ActivatedRoute);

  constructor(protected service: ApiService<Project>) { }

  project: Project | null = null;

  ngOnInit() {
    this.activedRoute.params.subscribe(({ id }) => {
      if (id) this.loadProject(id);
    });
  }

  private loadProject(id: string): void {
    console.log(`Loading project: ${id}`);
    this.service.request<Project>('get', `projects/${id}`)
      .subscribe({
        next: (project) => this.project = project,
        error: (err) => console.error('Failed to load project:', err),
      });
  }
}
