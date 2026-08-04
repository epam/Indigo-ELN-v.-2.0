import { CommonModule } from '@angular/common';
import { Component, HostBinding, inject, Input, OnInit } from '@angular/core';
import { MatIcon, MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';

export type IconSize = 'small' | 'medium' | 'large' | 'extra-large';

@Component({
  selector: 'eln-svg',
  standalone: true,
  imports: [CommonModule, MatIcon],
  templateUrl: './svg-icon.component.html',
  styleUrl: './svg-icon.component.scss',
})
export class SvgIconComponent implements OnInit {
  @Input() icon!: string;
  @Input() size: IconSize = 'medium';
  @Input() classList = '';
  @HostBinding('style.color') @Input() color = '';

  private iconRegistry = inject(MatIconRegistry);
  private sanitizer = inject(DomSanitizer);

  ngOnInit(): void {
    // Check if icon is a font icon (starts with indicon-) or SVG icon
    if (!this.icon.startsWith('indicon-')) {
      this.registerSvgIcon(this.icon);
    }
  }

  private registerSvgIcon(iconName: string): void {
    // The icon name resolves to a static SVG bundled in our own assets folder; the value is
    // set by developers via the component input and never carries user- or backend-supplied
    // data, so bypassing the sanitizer for this resource URL is safe.
    this.iconRegistry.addSvgIcon(iconName, this.sanitizer.bypassSecurityTrustResourceUrl(`assets/${iconName}.svg`)); // NOSONAR
  }

  get isFontIcon(): boolean {
    return this.icon.startsWith('indicon-');
  }

  get sizeClass(): string {
    return `icon-${this.size}`;
  }
}
