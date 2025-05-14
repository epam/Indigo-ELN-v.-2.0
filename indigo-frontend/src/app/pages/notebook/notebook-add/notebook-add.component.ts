import {
  AfterViewInit,
  Component,
  ElementRef,
  inject,
  Input,
  ViewChild,
} from '@angular/core';
import { ModalComponent } from '../../../../core/components/common/modal/modal.component';
import { InputComponent } from '../../../../core/components/common/input/input.component';
import { ButtonComponent } from '../../../../core/components/common/button/button.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import pell from 'pell';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../../../core/services/api.service';

@Component({
  standalone: true,
  selector: 'eln-notebook-add[projectId]',
  imports: [
    ModalComponent,
    InputComponent,
    ButtonComponent,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
  ],
  templateUrl: './notebook-add.component.html',
  styleUrls: ['./notebook-add.component.scss'],
})
export class NotebookAddComponent<T> implements AfterViewInit {
  @ViewChild('addNotebookModal') modalComponent!: ModalComponent;
  @ViewChild('description') description!: ElementRef;
  @Input() projectId: string = '';

  notebookName: string = '';
  notebookNameError: boolean = false;
  notebookDescription: string = '';
  editor: any;

  service: ApiService<T> = inject(ApiService);

  ngAfterViewInit() {
    if (this.description) {
      this.editor = pell.init({
        element: this.description.nativeElement,
        onChange: (html: string) => {
          this.notebookDescription = html;
        },
        styleWithCSS: true,
        actions: ['bold', 'italic', 'underline'],
        classes: {
          actionbar: 'pell-actionbar',
          button: 'pell-button',
          content: 'pell-content',
          selected: 'is-selected',
        },
      });
    }
  }

  async open() {
    return await this.modalComponent.open();
  }

  close() {
    this.modalComponent.close();
    this.notebookName = '';
    this.notebookNameError = false;
    this.notebookDescription = '';
    this.editor.content.innerHTML = '';
  }

  createNotebook() {
    if (this.notebookName) {
      this.service.setup('projects/' + this.projectId + '/notebooks');
      const notebookData = {
        name: this.notebookName,
        description: this.notebookDescription,
      };
      this.service.create(notebookData).subscribe({
        next: (response) => {
          this.modalComponent.close();
          this.notebookName = '';
          this.notebookNameError = false;
          this.notebookDescription = '';
          this.editor.content.innerHTML = '';
        },
        error: (error) => {
          console.error('Error creating notebook:', error);
        },
      });
    } else {
      this.notebookNameError = true;
    }
  }
}
