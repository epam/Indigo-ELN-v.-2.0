import { TwsxPipe } from '@/core/pipes/twsx.pipe';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'eln-experiment-tab-button',
  templateUrl: './experiment-tab-button.component.html',
  imports: [RouterLink, TwsxPipe, RouterLinkActive],
  styles: `
    :host {
      display: contents;
    }
  `,
})
export class ExperimentTabButtonComponent {
  @Output() clicked = new EventEmitter<void>();
  @Input() className: string;
  @Input() label: string;
  @Input() active = false;
  @Input() routerLink: string;
}
