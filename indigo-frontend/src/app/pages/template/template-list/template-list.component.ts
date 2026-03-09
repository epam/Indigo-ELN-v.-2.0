import { Component, inject, Input, OnDestroy } from '@angular/core';
import {
  ListHeaderComponent,
  SortChangeEvent,
} from '@core/components/common/list-header/list-header.component';
import { animate, style, transition, trigger } from '@angular/animations';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { ClassPickerPipe } from '@core/pipes/classPicker.pipe';
import { InfiniteLoaderComponent } from '@core/components/util/infinite-loader/infinite-loader.component';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { InfiniteScrollBase } from '@core/components/util/infinite-scroll.base';
import { MatDialog } from '@angular/material/dialog';
import { Subscription, take } from 'rxjs';
import { DropdownMenuItem } from '@core/components/common/dropdown-menu/dropdown-menu.i';
import { RouteAnimationType } from '@core/animations/route-animations';
import { TemplateItemComponent } from '@core/components/template/template-item/template-item.component';
import { ItemTemplate } from '@core/types/entities/template.i';
import { TemplateAddComponent } from '@pages/template/template-add/template-add.component';

@Component({
  selector: 'eln-template-list',
  templateUrl: './template-list.component.html',
  standalone: true,
  animations: [
    trigger('viewChange', [
      transition('grid <=> list', [
        style({ opacity: 0, transform: 'scale(0.95)' }),
        animate('200ms ease-out', style({ opacity: 1, transform: 'scale(1)' })),
      ]),
    ]),
  ],
  imports: [
    ListHeaderComponent,
    CommonModule,
    FormsModule,
    MatSlideToggleModule,
    ClassPickerPipe,
    InfiniteLoaderComponent,
    ButtonComponent,
    ListHeaderComponent,
    TemplateItemComponent,
  ],
})
export class TemplateListComponent
  extends InfiniteScrollBase<ItemTemplate>
  implements OnDestroy
{
  @Input() animationType!: RouteAnimationType;
  dialog = inject(MatDialog);
  selectedView: 'list';
  private refreshSub!: Subscription;

  headerSortOptions: DropdownMenuItem[] = [];

  constructor() {
    super();
    this.setup({
      loadUrl: 'templates',
      sortOptions: [
        {
          label: 'Sorting by: Earliest',
          value: 'createdAt',
          defaultOrder: 'EARLIEST',
        },
        {
          label: 'Sorting by: Latest',
          value: 'createdAt',
          defaultOrder: 'LATEST',
        },
      ],
      defaultSort: {
        sortBy: 'createdAt',
        sort: 'EARLIEST',
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

  ngOnDestroy(): void {
    this.refreshSub?.unsubscribe();
  }

  async openModal() {
    const ref = this.dialog.open(TemplateAddComponent);
    ref
      .afterClosed()
      .pipe(take(1))
      .subscribe((result) => {
        if (result === 'refresh') {
          this.refreshList();
        }
      });
  }

  onSearch(value: string) {
    this.search(value);
  }

  onSortChange(event: SortChangeEvent) {
    this.sort(event.sortBy, event.sort);
  }
  onMyEntitiesOnlyChange(value: boolean) {
    this.filters['createdByMe'] = value;
    this.reload();
  }
}
