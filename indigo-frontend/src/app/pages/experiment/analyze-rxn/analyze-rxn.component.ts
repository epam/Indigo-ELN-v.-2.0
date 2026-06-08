import { CommonModule } from '@angular/common';
import { Component, inject, Input, OnInit, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { Mutation, ReactionInputAnchor } from '@core/types/entities/experiments/mutation.i';
import { MatTab, MatTabGroup, MatTabLabel } from '@angular/material/tabs';
import { SampleSearchComponent } from '@pages/experiment/sample-search/sample-search.component';
import {
  Sample,
  SEARCH_CATALOG_MAPPING,
  SearchCatalogUI,
  StructuralSearchType,
} from '@core/types/entities/experiments/search.i';
import { MatRadioButton, MatRadioGroup } from '@angular/material/radio';
import { Reaction } from '@core/types/entities/experiments/experiment.i';
import { SamplesSearchLoader } from '@core/components/util/infinite-scroll-search';
import { ApiService } from '@core/services/api.service';
import { SampleSearchResultsComponent } from '@pages/experiment/sample-search-results/sample-search-results.component';
import { SlideInPanelService } from '@core/components/common/slide-in-panel/slide-in-panel.service';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

interface Tab {
  name: string;
  anchor: ReactionInputAnchor;
  resultCount?: number;
  loader: SamplesSearchLoader;
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
    ReactiveFormsModule,
    FormsModule,
    MatTabLabel,
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
  catalog: SearchCatalogUI = SearchCatalogUI.ALL;

  ngOnInit() {
    this.tabs = this.reaction.inputs
      .filter((input) => input.rxnPosition != null)
      .map((input) => {
        const search = this.unresolvedInputs[input.anchor];
        let loader: SamplesSearchLoader | null = null;
        if (search != null) {
          loader = new SamplesSearchLoader(this.apiService);
          loader.search({
            catalogs: SEARCH_CATALOG_MAPPING[this.catalog],
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
    if (!sample.id) {
      this.apiService.request<Sample>('post', '/samples/importFromSearch', sample).subscribe((response) => {
        this.doAddToExperiment(tab.anchor, response.id);
      });
    } else {
      this.doAddToExperiment(tab.anchor, sample.id);
    }
  }

  doAddToExperiment(anchor: ReactionInputAnchor, sampleID: UUID) {
    const mutation = {
      type: 'ResolveInputs',
      anchor: this.reaction.anchor,
      inputSamples: { [anchor]: sampleID },
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
