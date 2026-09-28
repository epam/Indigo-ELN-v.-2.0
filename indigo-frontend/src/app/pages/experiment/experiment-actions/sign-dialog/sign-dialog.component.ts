import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormDialogComponent } from '@core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@core/services/api.service';
import { MatDialogRef } from '@angular/material/dialog';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { catchError, map, of, shareReplay, tap } from 'rxjs';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';

interface SignatureTemplateRef {
  id: UUID;
  name: string;
}

@Component({
  selector: 'eln-sign-dialog',
  standalone: true,
  imports: [CommonModule, FormDialogComponent],
  templateUrl: './sign-dialog.component.html',
})
export class SignDialogComponent implements OnInit {
  private api = inject(ApiService);
  private dialogRef = inject(MatDialogRef<SignDialogComponent>);

  loading = signal(true);
  loadError = signal<string | null>(null);

  options$ = this.api.request<SignatureTemplateRef[]>('get', 'signatureTemplates').pipe(
    map((templates) => templates.map((t) => ({ value: t.id, label: t.name }))),
    tap(() => this.loading.set(false)),
    catchError(() => {
      this.loading.set(false);
      this.loadError.set('Failed to load signature templates');
      return of([]);
    }),
    shareReplay(1),
  );

  ngOnInit() {
    this.options$.subscribe();
  }

  fields: FormlyFieldConfig[] = [
    {
      type: 'dropdown',
      key: 'signatureTemplateId',
      props: {
        label: 'Signature Template',
        placeholder: 'Select a signature template',
        required: true,
        options: this.options$,
      },
    },
  ];

  onSubmit(value: { signatureTemplateId: string }): void {
    this.dialogRef.close(value.signatureTemplateId);
  }
}
