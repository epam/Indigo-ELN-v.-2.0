import { DropdownMenuItem } from '@/core/components/common/dropdown-menu/dropdown-menu.i';
import { ListHeaderComponent, SortChangeEvent } from '@/core/components/common/list-header/list-header.component';
import { InfiniteLoaderComponent } from '@/core/components/util/infinite-loader/infinite-loader.component';
import { InfiniteScrollBase } from '@/core/components/util/infinite-scroll.base';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Subject, Subscription } from 'rxjs';
import { SignatureItemComponent } from '@core/components/signature/signature-item/signature-item.component';
import { Document } from '@core/types/entities/document.i';

@Component({
  selector: 'eln-signature-list',
  templateUrl: './signature-list.component.html',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatSlideToggleModule,
    ClassPickerPipe,
    InfiniteLoaderComponent,
    ListHeaderComponent,
    SignatureItemComponent,
  ],
})
export class SignatureListComponent extends InfiniteScrollBase<Document> implements OnDestroy {
  destroy$ = new Subject<void>();
  dialog = inject(MatDialog);
  private refreshSub!: Subscription;

  headerSortOptions: DropdownMenuItem[] = [];
  username: string | null = null;

  constructor() {
    super();
    this.setup({
      loadUrl: '/api/signature/documents',
      sortOptions: [
        { label: 'Sort by: Earliest', value: 'EARLIEST' },
        { label: 'Sort by: Latest', value: 'LATEST' },
      ],
      defaultSort: {
        sort: 'LATEST',
      },
    });

    // Convert sort options to dropdown menu items
    this.headerSortOptions = this.getSortOptions().map((option) => ({
      label: `${option.label}`,
      value: option.value,
      icon: 'indicon-sort',
    }));
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSearch(value: string) {
    this.search(value);
  }

  onSortChange(event: SortChangeEvent) {
    this.sort(event.sort);
  }

  onMyEntitiesOnlyChange(value: boolean) {
    this.filters['waitingMySignature'] = value;
    this.reload();
  }
}
