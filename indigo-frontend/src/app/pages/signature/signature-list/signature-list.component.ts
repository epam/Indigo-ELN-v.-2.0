import { ButtonComponent } from '@/core/components/common/button/button.component';
import { DropdownMenuItem } from '@/core/components/common/dropdown-menu/dropdown-menu.i';
import { ListHeaderComponent, SortChangeEvent } from '@/core/components/common/list-header/list-header.component';
import { InfiniteLoaderComponent } from '@/core/components/util/infinite-loader/infinite-loader.component';
import { InfiniteScrollBase } from '@/core/components/util/infinite-scroll.base';
import { ClassPickerPipe } from '@/core/pipes/classPicker.pipe';
import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Subject, Subscription, take, takeUntil } from 'rxjs';
import { ExperimentAddComponent } from '../../experiment/experiment-add/experiment-add.component';
import { ProjectAddComponent } from '../../project/project-add/project-add.component';
import { SignatureItemComponent } from '@core/components/signature/signature-item/signature-item.component';
import { UserService } from '@core/services/user.service';
import { ExperimentForSignature } from '@core/types/entities/experiments/experiment-detail.i';

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
    ButtonComponent,
    ListHeaderComponent,
    SignatureItemComponent,
  ],
})
export class SignatureListComponent extends InfiniteScrollBase<ExperimentForSignature> implements OnInit, OnDestroy {
  destroy$ = new Subject<void>();
  dialog = inject(MatDialog);
  userService = inject(UserService);
  private refreshSub!: Subscription;

  headerSortOptions: DropdownMenuItem[] = [];
  username: string | null = null;

  constructor() {
    super();
    this.setup({
      loadUrl: 'signature/experiments/pending',
      sortOptions: [
        { label: 'Sort by: Earliest', value: 'createdAt', defaultOrder: 'EARLIEST' },
        { label: 'Sort by: Latest', value: 'createdAt', defaultOrder: 'LATEST' },
      ],
      defaultSort: {
        sortBy: 'createdAt',
        sort: 'LATEST',
      },
    });

    // Convert sort options to dropdown menu items
    this.headerSortOptions = this.getSortOptions().map((option) => ({
      label: `${option.label}`,
      value: `${option.value}:${option.defaultOrder}`,
      icon: 'indicon-sort',
    }));
  }

  refreshList(): void {
    this.reload();
  }

  ngOnInit(): void {
    this.userService.user$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      this.username = user['cognito:username'];
    });
  }

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
    this.destroy$.next();
    this.destroy$.complete();
  }

  async openModal() {
    const ref = this.dialog.open(ProjectAddComponent);
    ref
      .afterClosed()
      .pipe(take(1))
      .subscribe((result) => {
        if (result === 'refresh') {
          this.refreshList();
        }
      });
  }

  async openExperimentModal() {
    const ref = this.dialog.open(ExperimentAddComponent);
    ref
      .afterClosed()
      .pipe(take(1))
      .subscribe((result) => {
        if (result === 'refresh') {
          // do something after experiment is added
        }
      });
  }

  onSearch(value: string) {
    this.search(value);
  }

  onSortChange(event: SortChangeEvent) {
    this.sort(event.sortBy, event.sort);
  }
}
