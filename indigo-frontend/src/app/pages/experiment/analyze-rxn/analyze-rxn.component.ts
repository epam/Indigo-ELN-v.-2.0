import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { Mutation, ReactionInputAnchor } from '@core/types/entities/experiments/mutation.i';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { SampleSearchComponent } from '@pages/experiment/sample-search/sample-search.component';
import { FindSamplesRequest, Sample, StructuralSearchType } from '@core/types/entities/experiments/search.i';
import { MatRadioButton, MatRadioGroup } from '@angular/material/radio';
import { Reaction } from '@core/types/entities/experiments/experiment.i';
import { InfiniteSearchLoader } from '@core/components/util/infinite-scroll-search';
import { ApiService } from '@core/services/api.service';
import { SampleSearchResultsComponent } from '@pages/experiment/sample-search-results/sample-search-results.component';
import { SlideInPanelService } from '@core/components/common/slide-in-panel/slide-in-panel.service';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';

interface Tab {
  name: string;
  anchor: ReactionInputAnchor;
  resultCount?: number;
  loader: InfiniteSearchLoader<FindSamplesRequest, Sample>;
}

@Component({
  selector: 'eln-experiment-item',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatTab,
    MatTabGroup,
    MatRadioButton,
    MatRadioGroup,
    SampleSearchResultsComponent,
  ],
  templateUrl: './analyze-rxn.component.html',
})
export class AnalyzeRxnComponent implements OnInit {
  @Input({ required: true }) reaction: Reaction;
  @Input({ required: true }) unresolvedInputs: Record<ReactionInputAnchor, string>;
  @ViewChild(MatTabGroup) tabGroup!: MatTabGroup;
  @ViewChildren(SampleSearchComponent)
  searchComponents!: QueryList<SampleSearchComponent>;
  apiService = inject(ApiService);
  slideInPanelService = inject(SlideInPanelService);
  experimentDetailService = inject(ExperimentDetailService);
  notificationService = inject(NotificationService);

  tabs: Tab[];
  selectedTab: number;

  ngOnInit() {
    this.tabs = this.reaction.inputs
      .filter((input) => input.rxnPosition != null)
      .map((input) => {
        const search = this.unresolvedInputs[input.anchor];
        let loader = null;
        if (search != null) {
          loader = new InfiniteSearchLoader<FindSamplesRequest, Sample>((searchParams, pageNo) =>
            this.apiService.request('post', `samples/search?pageNo=${pageNo}&pageSize=20`, searchParams),
          );
          loader.search({
            structure: {
              type: StructuralSearchType.SUBSTRUCTURE,
              query: search,
            },
          });
        }
        return {
          name: input.compound.formula,
          anchor: input.anchor,
          resultCount: null,
          loader,
        };
      });
    this.selectedTab = this.tabs.findIndex((x) => x.loader != null);
  }

  close() {
    this.slideInPanelService.close();
  }

  addToExperiment(tab: Tab, sample: Sample) {
    const mutation = {
      type: 'ResolveInputs',
      anchor: this.reaction.anchor,
      inputSamples: { [tab.anchor]: sample.id },
    } as Mutation;
    this.experimentDetailService.updateDataModel(mutation).subscribe(() => {
      this.notificationService.notify({
        type: NotificationType.Info,
        message: 'Model updated with new sample',
        isInline: false,
      });
    });
  }
}
