import { AfterViewInit, Component, ElementRef, EventEmitter, Output, ViewChild } from '@angular/core';
import { Ketcher } from 'ketcher-core';

@Component({
  selector: 'eln-ketcher',
  imports: [],
  templateUrl: './ketcher.component.html',
  styles: [
    `
      :host {
        display: block;
        width: 100%;
        height: 100%;
      }
      iframe {
        width: 100% !important;
        height: 100% !important;
        border: none;
        display: block;
      }
    `,
  ],
})
export class KetcherComponent implements AfterViewInit {
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

  getRxnOrMolfile(isReaction: boolean | null): Promise<string> {
    const actualReaction = isReaction != null ? isReaction : this.ketcher.containsReaction();
    return actualReaction ? this.ketcher.getRxn() : this.ketcher.getMolfile();
  }

  generateImage(molOrRxnfile: string): Promise<Blob> {
    return this.ketcher.generateImage(molOrRxnfile, { outputFormat: 'svg' });
  }

  containsReaction(): boolean {
    return this.ketcher.containsReaction();
  }
}
