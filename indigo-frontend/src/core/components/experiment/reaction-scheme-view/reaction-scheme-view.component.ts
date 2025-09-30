import { Component, inject, signal, computed, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StructureEditorModalComponent } from '../structure-editor-modal/structure-editor-modal.component';
import { ApiService } from '@/core/services/api.service';
import { MatDialog } from '@angular/material/dialog';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';

@Component({
  selector: 'eln-reaction-scheme-view',
  standalone: true,
  imports: [CommonModule, ButtonComponent],
  templateUrl: './reaction-scheme-view.component.html',
})
export class ReactionSchemeViewComponent {
  @Input() experimentId: string | null = null;

  apiService = inject(ApiService);
  dialog = inject(MatDialog);

  // Simple state for loaded data
  loadedDataModel = signal<ExperimentDetail | null>(null);
  showDebugView = signal(false);

  // Computed property for UI
  hasDataModel = computed(() => this.loadedDataModel() !== null);

  async openChemicalEditor(): Promise<void> {
    if (!this.experimentId) {
      console.warn('No experiment ID available');
      return;
    }

    // Load existing data before opening the modal
    await this.loadExistingDataModel(this.experimentId);

    const dialogRef = this.dialog.open(StructureEditorModalComponent, {
      width: '90vw',
      height: '80vh',
      maxWidth: '1200px',
      maxHeight: '800px',
      disableClose: false,
      data: {
        height: '500px',
        width: '100%'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log('Modal closed with result:', result);
        // TODO: Handle result when iframe communication is implemented
      }
    });
  }

  private async loadExistingDataModel(experimentId: string): Promise<void> {
    try {
      console.log('Loading existing data model for experiment:', experimentId);

      this.apiService.request<ExperimentDetail>('get', `experiments/${experimentId}/datamodel`)
        .subscribe({
          next: (response: ExperimentDetail) => {
            console.log('Existing data model loaded:', response);
            this.loadedDataModel.set(response);
          },
          error: (error) => {
            console.warn('No existing data model found or error loading:', error);
            this.loadedDataModel.set(null);
          }
        });

    } catch (error) {
      console.error('Error loading existing data model:', error);
      this.loadedDataModel.set(null);
    }
  }

  toggleDebugView(): void {
    this.showDebugView.update(current => !current);
  }
}