import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { Dialog, DialogModule, DialogRef } from '@angular/cdk/dialog';
import { CommonModule } from '@angular/common';
import {
  Component,
  ContentChild,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
  TemplateRef,
  ViewChild,
  inject,
} from '@angular/core';

type ModalSize = 'small' | 'medium' | 'large' | 'auto';

@Component({
  selector: 'app-modal',
  standalone: true,
  imports: [CommonModule, DialogModule, ClassPickerPipe],
  templateUrl: './modal.component.html',
})
export class ModalComponent implements OnDestroy {
  private dialog = inject(Dialog);
  private dialogRef: DialogRef<unknown> | null = null;

  @ViewChild('modalTemplate') modalTemplate!: TemplateRef<unknown>;
  @ContentChild('modalHeader') modalHeader: TemplateRef<unknown> | null = null;
  @ContentChild('modalContent') modalContent: TemplateRef<unknown> | null =
    null;
  @ContentChild('modalFooter') modalFooter: TemplateRef<unknown> | null = null;

  @Input() id = '';
  @Input() title = '';
  @Input() size: ModalSize = 'auto';
  @Input() showCloseButton = true;
  @Input() closeOnBackdropClick = true;
  @Input() closeOnEsc = true;

  @Output() closed = new EventEmitter<unknown>();
  @Output() opened = new EventEmitter<void>();

  ngOnDestroy(): void {
    this.close();
  }

  open(): void {
    if (this.dialogRef) {
      return;
    }

    this.dialogRef = this.dialog.open(this.modalTemplate, {
      backdropClass: 'bg-neutral-1000/50',
      hasBackdrop: true,
      disableClose: !this.closeOnBackdropClick,
      closeOnDestroy: true,
    });

    this.dialogRef.closed.subscribe((result) => {
      this.dialogRef = null;
      this.closed.emit(result);
    });

    this.opened.emit();
  }

  close(result?: unknown): void {
    if (this.dialogRef) {
      this.dialogRef.close(result);
      this.dialogRef = null;
    }
  }

  onBackdropClick(event: MouseEvent): void {
    if (
      this.closeOnBackdropClick &&
      (event.target as HTMLElement).classList.contains('modal-backdrop')
    ) {
      this.close();
    }
  }

  onKeydown(event: KeyboardEvent): void {
    if (this.closeOnEsc && event.key === 'Escape') {
      this.close();
    }
  }
}
