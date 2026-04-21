import { Directive, HostListener, inject } from '@angular/core';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';

@Directive({
  selector: '[appUndoRedo]',
  standalone: true,
})
export class UndoRedoDirective {
  private experimentDetailService = inject(ExperimentDetailService);

  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent) {
    if (event.target instanceof HTMLInputElement || event.target instanceof HTMLTextAreaElement) {
      // don't interfere with input component's built-in undo/redo
      return;
    }
    const ctrl = event.ctrlKey || event.metaKey; // Ctrl on Windows/Linux or Cmd on Mac
    if (ctrl && event.key === 'z' && !event.shiftKey) {
      // Ctrl+Z or Cmd+Z
      event.preventDefault();
      this.experimentDetailService.updateDataModel({ type: 'Undo' }).subscribe();
    } else if (ctrl && (event.key === 'y' || (event.key === 'z' && event.shiftKey))) {
      // Ctrl+Y (Windows) or Cmd+Shift+Z (Mac)
      event.preventDefault();
      this.experimentDetailService.updateDataModel({ type: 'Redo' }).subscribe();
    }
  }
}
