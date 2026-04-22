import { Injectable, TemplateRef } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ProjectOverviewWidgetSlot } from '../types/project-overview-widget.i';

@Injectable({
  providedIn: 'root',
})
export class ProjectsOverviewWidgetService {
  private readonly slotTemplates = new Map<ProjectOverviewWidgetSlot, Set<TemplateRef<unknown>>>();

  private readonly tabTemplatesSubject = new BehaviorSubject<TemplateRef<unknown>[]>([]);

  private readonly buttonTemplatesSubject = new BehaviorSubject<TemplateRef<unknown>[]>([]);

  private lastBackup: {
    tabs: Set<TemplateRef<unknown>>;
    buttons: Set<TemplateRef<unknown>>;
  } | null = null;

  readonly tabTemplates$: Observable<TemplateRef<unknown>[]> = this.tabTemplatesSubject.asObservable();

  readonly buttonTemplates$: Observable<TemplateRef<unknown>[]> = this.buttonTemplatesSubject.asObservable();

  constructor() {
    this.slotTemplates.set('tab', new Set());
    this.slotTemplates.set('button', new Set());
  }

  addTemplate(template: TemplateRef<unknown>, slot: ProjectOverviewWidgetSlot): void {
    const templates = this.slotTemplates.get(slot)!;

    // Only add if not already present
    if (!templates.has(template)) {
      templates.add(template);
      this.emitTemplates(slot);
    }
  }

  removeTemplate(template: TemplateRef<unknown>, slot: ProjectOverviewWidgetSlot): void {
    const templates = this.slotTemplates.get(slot)!;
    if (templates.delete(template)) {
      this.emitTemplates(slot);
    }
  }

  restoreLatest(): void {
    if (!this.lastBackup) {
      return;
    }

    this.slotTemplates.set('tab', new Set(this.lastBackup.tabs));
    this.slotTemplates.set('button', new Set(this.lastBackup.buttons));

    this.emitTemplates('tab');
    this.emitTemplates('button');
  }

  private emitTemplates(slot: ProjectOverviewWidgetSlot): void {
    const templates = Array.from(this.slotTemplates.get(slot)!);
    if (slot === 'tab') {
      this.tabTemplatesSubject.next(templates);
    } else {
      this.buttonTemplatesSubject.next(templates);
    }
  }

  private clearAllTemplates(): void {
    // Backup current state
    this.lastBackup = {
      tabs: new Set(this.slotTemplates.get('tab')!),
      buttons: new Set(this.slotTemplates.get('button')!),
    };

    // Clear all templates
    this.slotTemplates.get('tab')!.clear();
    this.slotTemplates.get('button')!.clear();
    this.tabTemplatesSubject.next([]);
    this.buttonTemplatesSubject.next([]);
  }
}
