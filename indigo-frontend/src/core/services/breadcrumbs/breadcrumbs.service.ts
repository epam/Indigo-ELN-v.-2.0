import { Injectable, inject } from '@angular/core';
import { forkJoin } from 'rxjs';

import { ApiService } from '@/core/services/api.service';
import { Project } from '@/core/types/entities/project.i';

interface NamedEntity {
  id: string;
  name: string;
}

@Injectable({
  providedIn: 'root',
})
export class BreadcrumbService {
  private readonly api = inject(ApiService);

  getProject(projectId: string) {
    return this.api.request<Project>('get', `projects/${projectId}`);
  }

  getNotebook(notebookId: string) {
    return this.api.request<NamedEntity>('get', `notebooks/${notebookId}`);
  }

  getExperiment(experimentId: string) {
    return this.api.request<NamedEntity>('get', `experiments/${experimentId}`);
  }

  getNotebookData(projectId: string, notebookId: string) {
    return forkJoin({
      project: this.getProject(projectId),
      notebook: this.getNotebook(notebookId),
    });
  }

  getExperimentData(
    projectId: string,
    notebookId: string,
    experimentId: string,
  ) {
    return forkJoin({
      project: this.getProject(projectId),
      notebook: this.getNotebook(notebookId),
      experiment: this.getExperiment(experimentId),
    });
  }
}