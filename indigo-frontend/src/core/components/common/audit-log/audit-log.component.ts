import { Component, inject, Input, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../card/card.component';
import { finalize, Observable } from 'rxjs';
import { NgSelectModule } from '@ng-select/ng-select';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  ColumnDefDirective,
  ExpandableTableComponent,
} from '@core/components/common/expandable-table/expandable-table.component';
import { RevisionSummary } from '@core/types/entities/revision.i';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
  selector: 'eln-audit-log',
  templateUrl: './audit-log.component.html',
  styles: `
    .old {
      background-color: #f8d7da;
    }
    .old svg {
      background-color: #f8d7da;
    }
    .new {
      background-color: #d4edda;
    }
    .new svg {
      background-color: #d4edda;
    }
    .key {
      font-weight: bold;
    }
    .comment {
      font-weight: normal;
      font-style: italic;
      background-color: white;
    }
    .ev-fixed {
      color: purple;
    }
    .ev-default {
      color: darkgray;
    }
    .ev-user-entered {
      font-weight: bold;
    }
    svg {
      height: 100px;
      width: auto;
      display: inline;
    }
  `,
  encapsulation: ViewEncapsulation.None,
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    NgSelectModule,
    FormsModule,
    MatButtonModule,
    ExpandableTableComponent,
    ColumnDefDirective,
    ButtonComponent,
    MatTooltip,
  ],
})
export class AuditLogComponent implements OnInit {
  @Input({ required: true }) entityId: string;
  @Input({ required: true }) loader: (string) => Observable<RevisionSummary[]>;
  @Input({ required: true }) diffLoader: (RevisionSummary) => Observable<string>;

  @ViewChild(ExpandableTableComponent) expandableTable: ExpandableTableComponent<RevisionSummary>;

  domSanitizer = inject(DomSanitizer);

  loading = false;
  revisions: RevisionSummary[] = [];
  diffs = new Map<RevisionSummary, SafeHtml>();
  hasDetails = (revision: RevisionSummary): boolean => !!revision.details?.length;

  ngOnInit(): void {
    this.loading = true;
    this.loader(this.entityId)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe((result) => {
        this.revisions = result;
        this.loading = false;
      });
  }

  showDiff(revision: RevisionSummary): void {
    if (!this.diffs.has(revision)) {
      this.diffLoader(revision).subscribe((html) => {
        this.diffs.set(revision, this.domSanitizer.bypassSecurityTrustHtml(html));
        this.expandableTable.expandRow(revision);
      });
    }
  }
}
