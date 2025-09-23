import { AfterViewInit, Component, ElementRef, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { Ketcher } from 'ketcher-core';

@Component({
  selector: 'eln-ketcher',
  imports: [],
  templateUrl: './ketcher.component.html',
  styleUrl: './ketcher.component.scss'
})
export class KetcherComponent implements AfterViewInit {
  @Input() width = 784;

  @Input() height = 624;

  @Output() ketcherLoad = new EventEmitter<Ketcher>();

  @ViewChild('ketcher') private iframe!: ElementRef;

  private ketcher!: Ketcher;

  ngAfterViewInit() {
    this.iframe.nativeElement.addEventListener('load', () => {
      this.initializeKetcherEditor();
    });
  }

  private initializeKetcherEditor() {
    const iframeCtx = this.iframe.nativeElement.contentWindow;

    if (iframeCtx.ketcher) {
      this.ketcher = iframeCtx.ketcher;
      this.ketcherLoad.emit(this.ketcher);
    } else {
      setTimeout(() => this.initializeKetcherEditor(), 100);
    }
  }

}
