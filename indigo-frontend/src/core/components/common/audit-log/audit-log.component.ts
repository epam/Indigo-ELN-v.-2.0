import { Component, inject, Injector, Input, OnInit, ResourceRef, ViewChild, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '../card/card.component';
import { finalize, Observable, tap } from 'rxjs';
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
import { rxResource } from '@angular/core/rxjs-interop';
import { map } from 'rxjs/operators';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'eln-audit-log',
  templateUrl: './audit-log.component.html',
  styles: `
    .audit-log .old {
      background-color: #f8d7da;
    }
    .audit-log .old svg {
      background-color: #f8d7da;
    }
    .audit-log .new {
      background-color: #d4edda;
    }
    .audit-log .new svg {
      background-color: #d4edda;
    }
    .audit-log .key {
      font-weight: bold;
    }
    .audit-log .comment {
      font-weight: normal;
      font-style: italic;
      background-color: white;
    }
    .audit-log .warning {
      font-weight: bold;
      font-style: italic;
      color: white;
      background-color: darkred;
    }
    .audit-log .ev-fixed {
      color: purple;
    }
    .audit-log .ev-default {
      color: darkgray;
    }
    .audit-log .ev-user-entered {
      font-weight: bold;
    }
    .audit-log svg {
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
    MatProgressSpinner,
  ],
})
export class AuditLogComponent implements OnInit {
  @Input({ required: true }) entityId: string;
  @Input({ required: true }) loader: (string) => Observable<RevisionSummary[]>;
  @Input({ required: true }) diffLoader: (RevisionSummary) => Observable<string>;

  @ViewChild(ExpandableTableComponent) expandableTable: ExpandableTableComponent<RevisionSummary>;

  domSanitizer = inject(DomSanitizer);
  injector = inject(Injector);

  loading = false;
  revisions: RevisionSummary[] = [];
  diffs = new Map<RevisionSummary, ResourceRef<SafeHtml>>();
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
      this.diffs.set(
        revision,
        rxResource({
          injector: this.injector,
          loader: () =>
            this.diffLoader(revision).pipe(
              map((html) => this.domSanitizer.bypassSecurityTrustHtml(html)),
              tap({
                error: () => this.diffs.delete(revision),
              }),
            ),
        }),
      );
      this.expandableTable.expandRow(revision);
    }
  }
}
