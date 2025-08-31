import { Component, inject, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Experiment } from '@core/types/entities/experiment.i';
import { ExperimentService } from '@core/services/experiment.service';
import {ExperimentModel, ReactionInput, ReactionInputRole} from '@core/types/entities/experiment-model.i';
import { MatCheckbox } from '@angular/material/checkbox';
import { EnteredValueComponent } from '@core/components/experiment/entered-value/entered-value.component';
import {
  DENSITY_UNITS, EnteredValue, REACTION_INPUT_ROLES,
  MOL_UNITS,
  MOL_WEIGHT_UNITS,
  MOLARITY_UNITS,
  NO_UNITS,
  VOLUME_UNITS,
  WEIGHT_UNITS,
} from '@core/types/entities/values.i';
import {DropdownValueComponent} from '@core/components/experiment/dropdown-value/dropdown-value.component';

@Component({
  selector: 'eln-component-stoichiometry-table',
  templateUrl: './component-stoichiometry-table.component.html',
  imports: [MatCheckbox, EnteredValueComponent, DropdownValueComponent],
  styles: `
    .inputs-container {
      width: 100%; /* Container stays within parent */
      max-width: 100%; /* Prevents container from expanding */
      overflow-x: auto;
      overflow-y: visible;
      border: 1px solid #ddd; /* Optional: visual boundary */
    }
    table {
      width: auto;
      min-width: 100%;
      table-layout: fixed;
    }
    table td,
    table th {
      width: 200px;
      min-width: 200px;
      max-width: 200px;
      white-space: nowrap;
      text-align: center;
    }
  `,
})
export class ComponentStoichiometryTableComponent implements OnInit {
  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  model: ExperimentModel | null;
  showReactantsReagentsSolvents: boolean;
  showReactionProducts: boolean;

  ngOnInit() {
    this.experimentService.model$.subscribe((model) => {
      this.model = model;
    });
  }

  setRole(row: ReactionInput, value: ReactionInputRole) {
    this.experimentService.mutateModel({
      type: 'SetInputRole',
      anchor: row.anchor,
      role: value,
    })
  }

  setWeight(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputWeight',
      anchor: row.samples[0].anchor,
      weight: value?.value,
      unit: value?.unit,
    });
  }

  setVolume(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputVolume',
      anchor: row.samples[0].anchor,
      volume: value?.value,
      unit: value?.unit,
    });
  }

  setMol(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputMol',
      anchor: row.anchor,
      mol: value?.value,
      unit: value?.unit,
    });
  }

  setEQ(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputEQ',
      anchor: row.anchor,
      eq: value?.value,
    });
  }

  setDensity(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputDensity',
      anchor: row.samples[0].anchor,
      density: value?.value,
      unit: value?.unit,
    });
  }

  setMolarity(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputMolarity',
      anchor: row.samples[0].anchor,
      molarity: value?.value,
      unit: value?.unit,
    });
  }

  setPurity(row: ReactionInput, value: EnteredValue) {
    this.experimentService.mutateModel({
      type: 'SetInputPurity',
      anchor: row.samples[0].anchor,
      purity: value?.value,
    });
  }

  protected readonly MOL_WEIGHT_UNITS = MOL_WEIGHT_UNITS;
  protected readonly WEIGHT_UNITS = WEIGHT_UNITS;
  protected readonly VOLUME_UNITS = VOLUME_UNITS;
  protected readonly MOL_UNITS = MOL_UNITS;
  protected readonly NO_UNITS = NO_UNITS;
  protected readonly DENSITY_UNITS = DENSITY_UNITS;
  protected readonly MOLARITY_UNITS = MOLARITY_UNITS;
  protected readonly REACTION_INPUT_ROLES = REACTION_INPUT_ROLES;
}
