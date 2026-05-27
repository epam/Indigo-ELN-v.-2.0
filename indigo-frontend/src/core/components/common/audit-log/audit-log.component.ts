import { Component, Input, OnInit, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../card/card.component';
import { finalize, Observable } from 'rxjs';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';
import {
  ColumnDefDirective,
  ExpandableTableComponent,
} from '@core/components/common/expandable-table/expandable-table.component';
import { Revision } from '@core/types/entities/revision.i';

@Component({
  selector: 'eln-audit-log',
  templateUrl: './audit-log.component.html',
  styles: `
    .old {
      background-color: #f8d7da;
    }
    .new {
      background-color: #d4edda;
    }
  `,
  encapsulation: ViewEncapsulation.None,
  standalone: true,
  imports: [CommonModule, CardComponent, NgSelectModule, FormsModule, ExpandableTableComponent, ColumnDefDirective],
})
export class AuditLogComponent implements OnInit {
  @Input() entityId: string;
  @Input() loader: (string) => Observable<Revision[]>;

  loading = false;
  revisions: Revision[] = [];

  ngOnInit(): void {
    this.loading = true;
    this.loader(this.entityId)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe((result) => {
        this.revisions = result;
        this.loading = false;
      });
  }
}
