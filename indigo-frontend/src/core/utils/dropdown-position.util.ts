import { ElementRef } from '@angular/core';

export interface DropdownPositionConfig {
  minSpaceBelow?: number;
  viewportMargin?: number;
}

export interface DropdownPosition {
  direction: 'up' | 'down';
  alignment: 'left' | 'right';
}

const DEFAULT_CONFIG: DropdownPositionConfig = {
  minSpaceBelow: 200,
  viewportMargin: 10,
};

export function calculatePosition(
  containerRef: ElementRef,
  dropdownRef: ElementRef,
  config: DropdownPositionConfig = DEFAULT_CONFIG,
): DropdownPosition {
  const position: DropdownPosition = {
    direction: 'down',
    alignment: 'left',
  };

  if (!containerRef || !dropdownRef) return position;

  const containerRect = containerRef.nativeElement.getBoundingClientRect();
  const dropdownRect = dropdownRef.nativeElement.getBoundingClientRect();

  // Check vertical position
  const spaceBelow = window.innerHeight - containerRect.bottom;
  const spaceAbove = containerRect.top;

  if (
    spaceBelow < (config.minSpaceBelow || DEFAULT_CONFIG.minSpaceBelow!) &&
    spaceAbove > spaceBelow
  ) {
    position.direction = 'up';
  }

  // Check horizontal position
  const viewportWidth = window.innerWidth;
  const rightEdge = containerRect.left + dropdownRect.width;

  if (
    rightEdge >
    viewportWidth - (config.viewportMargin || DEFAULT_CONFIG.viewportMargin!)
  ) {
    position.alignment = 'right';
  }

  return position;
}
