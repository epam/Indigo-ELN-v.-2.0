import { TwsxPipe } from '@/core/pipes/twsx.pipe';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'eln-project-tab-button',
  templateUrl: './project-tab-button.component.html',
  imports: [RouterLink, TwsxPipe, RouterLinkActive],
  styles: `
    :host {
      display: contents;
    }
  `,
})
export class ProjectTabButtonComponent {
  @Output() clicked = new EventEmitter<void>();
  @Input() className: string;
  @Input() label: string;
  @Input() active = false;
  @Input() routerLink: string;
}
