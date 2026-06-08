import { Component, DestroyRef, forwardRef, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AutocompleteSelectComponent } from '@core/components/common/autocomplete-select/autocomplete-select.component';
import { UserRef } from '@core/types/entities/user.i';
import { Observable } from 'rxjs';
import { ApiService } from '@core/services/api.service';
import { AbstractControl, FormControl, FormGroup, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { DelegatingControlBase } from '@core/components/common/delegating-control/delegating-control-base.component';
import { HttpParams } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'eln-user-select',
  templateUrl: './user-select.component.html',
  imports: [CommonModule, AutocompleteSelectComponent, ReactiveFormsModule],
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => UserSelectComponent),
      multi: true,
    },
  ],
})
export class UserSelectComponent extends DelegatingControlBase<UserRef> implements OnInit {
  form = new FormGroup({
    search: new FormControl<UserRef | null>(null),
  });

  private api = inject(ApiService);
  private destroyRef = inject(DestroyRef);

  ngOnInit() {
    this.form
      .get('search')
      .valueChanges.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => this.triggerChange(value));
  }

  search(query: string): Observable<UserRef[]> {
    return this.api.request<UserRef[]>('get', 'users/suggest', {
      params: new HttpParams().set('search', query),
    });
  }

  displayFn(user: UserRef | null): string {
    return user ? `${user.displayName} <${user.username}>` : '';
  }

  protected getControlsToDisable(): AbstractControl[] {
    return Object.values(this.form.controls);
  }

  setValue(obj: UserRef | null): void {
    this.form.get('search').setValue(obj);
  }
}
