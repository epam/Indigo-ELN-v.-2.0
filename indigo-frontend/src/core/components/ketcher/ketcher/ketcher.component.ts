import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  Output,
  ViewChild,
} from '@angular/core';
import { renderKetcherEditor, unmountketcherEditor } from '../ketcher-wrapper';

@Component({
  selector: 'eln-ketcher',
  standalone: true,
  templateUrl: './ketcher.component.html',
  styleUrls: ['./ketcher.component.scss'],
})
export class KetcherComponent implements AfterViewInit, OnDestroy {
  @ViewChild('ketcherContainer', { static: true }) containerRef!: ElementRef;

  @Input() value?: string;
  @Output() valueChange = new EventEmitter<string>();

  ngAfterViewInit(): void {
    setTimeout(() => {
      renderKetcherEditor(this.containerRef.nativeElement, {
        initialValue: this.value,
        onStructureChange: (struct) => {
          this.valueChange.emit(struct);
        },
      });
    }, 100);
  }

  ngOnDestroy(): void {
    unmountketcherEditor(this.containerRef.nativeElement);
  }
}
