import { Directive, Input, OnDestroy, TemplateRef } from '@angular/core';
import { ProjectsOverviewWidgetService } from '../services/projects-overview-widget.service';
import { ProjectOverviewWidgetSlot } from '../types/project-overview-widget.i';

@Directive({
  selector: '[projectOverviewWidget]',
})
export class ProjectOverviewWidgetDirective implements OnDestroy {
  private currentSlot: ProjectOverviewWidgetSlot = 'tab';

  constructor(
    private readonly templateRef: TemplateRef<unknown>,
    private readonly projectsOverviewWidgetService: ProjectsOverviewWidgetService,
  ) {
    this.addTemplateToSlot('tab');
  }

  @Input() set projectOverviewWidget(slot: ProjectOverviewWidgetSlot) {
    if (this.currentSlot !== slot) {
      this.projectsOverviewWidgetService.removeTemplate(
        this.templateRef,
        this.currentSlot,
      );

      this.addTemplateToSlot(slot);
      this.currentSlot = slot;
    }
  }

  private addTemplateToSlot(slot: ProjectOverviewWidgetSlot): void {
    this.projectsOverviewWidgetService.addTemplate(this.templateRef, slot);
  }

  ngOnDestroy(): void {
    this.projectsOverviewWidgetService.removeTemplate(
      this.templateRef,
      this.currentSlot,
    );
  }
}
