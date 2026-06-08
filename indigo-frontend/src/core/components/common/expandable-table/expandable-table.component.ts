import { CommonModule } from '@angular/common';
import {
  AfterContentInit,
  Component,
  ContentChild,
  ContentChildren,
  Directive,
  Input,
  QueryList,
  TemplateRef,
} from '@angular/core';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { animate, state, style, transition, trigger } from '@angular/animations';

@Directive({
  selector: '[elnColumnDef]',
  standalone: true,
})
export class ColumnDefDirective {
  @Input('elnColumnDef') name: string;
  @Input({ required: true }) displayName: string;

  constructor(public template: TemplateRef<unknown>) {}
}

@Component({
  selector: 'eln-expandable-table',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatIconModule, MatButtonModule],
  templateUrl: './expandable-table.component.html',
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class ExpandableTableComponent<T> implements AfterContentInit {
  @Input({ required: true }) dataSource: T[] | MatTableDataSource<T>;
  @Input() expandable: (row: T) => boolean = () => true;
  @ContentChild('details') details: TemplateRef<unknown>;
  @ContentChildren(ColumnDefDirective)
  columnDefs!: QueryList<ColumnDefDirective>;
  columnNames: string[];

  expandedElements = new Set<T>();

  ngAfterContentInit() {
    this.columnNames = ['expand', ...this.columnDefs.map((c) => c.name)];
  }

  expandRow(element: T): void {
    this.expandedElements.add(element);
  }

  collapseRow(element: T): void {
    this.expandedElements.delete(element);
  }
  toggleRow(element: T): void {
    if (this.expandedElements.has(element)) {
      this.collapseRow(element);
    } else {
      this.expandRow(element);
    }
  }
}
