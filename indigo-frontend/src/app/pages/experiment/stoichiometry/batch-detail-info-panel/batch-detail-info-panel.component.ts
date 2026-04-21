import { Component, effect, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CdkAccordionModule } from '@angular/cdk/accordion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Reaction, ReactionOutput, ReactionOutputSample } from '@core/types/entities/experiments/experiment.i';
import {
  ExternalSupplier,
  MeltingPoint,
  PurityCalculation,
  ResidualSolvent,
  SolubidityInSolvent,
} from '@core/types/entities/experiments/experiment-shared.i';
import { EnteredValue } from '@core/types/entities/values.i';
import { BuiltInDictionary, DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { ChipListComponent } from '../shared/chip-list/chip-list.component';
import { DictionarySelectComponent } from '@core/components/common/dictionary-select/dictionary-select.component';
import { EnteredValueComponent } from '@core/components/experiment/entered-value/entered-value.component';

@Component({
  selector: 'eln-batch-detail-info-panel',
  templateUrl: './batch-detail-info-panel.component.html',
  styleUrl: './batch-detail-info-panel.component.scss',
  imports: [
    CommonModule,
    CdkAccordionModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    ReactiveFormsModule,
    ChipListComponent,
    DictionarySelectComponent,
    EnteredValueComponent,
  ],
  standalone: true,
})
export class BatchDetailInfoPanelComponent {
  sample = input.required<ReactionOutputSample>();
  output = input.required<ReactionOutput>();
  reaction = input.required<Reaction>();

  // Expose enum for template
  readonly BuiltInDictionary = BuiltInDictionary;

  // Form for editable fields
  form = new FormGroup({
    source: new FormControl<DictionaryItemRef | null>(null),
    sourceDetails: new FormControl<DictionaryItemRef | null>(null),
    stereoisomerCode: new FormControl<DictionaryItemRef | null>(null),
    componentState: new FormControl<DictionaryItemRef | null>(null),
    compoundProtection: new FormControl<DictionaryItemRef[] | null>(null),
    healthHazards: new FormControl<DictionaryItemRef[] | null>(null),
    handlingPrecautions: new FormControl<DictionaryItemRef[] | null>(null),
    storageInstructions: new FormControl<DictionaryItemRef[] | null>(null),
  });

  // Additional Information accordion state
  additionalInfoExpanded = false;

  constructor() {
    // Sync form with input data
    effect(() => {
      const sampleData = this.sample();
      const compoundData = this.output().compound;

      this.form.patchValue(
        {
          source: sampleData.source || null,
          sourceDetails: sampleData.sourceDetails || null,
          stereoisomerCode: compoundData?.stereoisomerCode || null,
          componentState: sampleData.componentState || null,
          compoundProtection: sampleData.compoundProtection || null,
          healthHazards: sampleData.healthHazards || null,
          handlingPrecautions: sampleData.handlingPrecautions || null,
          storageInstructions: sampleData.storageInstructions || null,
        },
        { emitEvent: false },
      );
    });

    // TODO: Handle form changes and emit to parent for saving
    // this.form.valueChanges.pipe(...).subscribe(...);
  }

  formatMeltingPoint(mp?: MeltingPoint): string {
    if (!mp) return '—';
    const parts = [];
    if (mp.lower) parts.push(`${mp.lower}°C`);
    if (mp.upper) parts.push(`${mp.upper}°C`);
    if (parts.length === 0) return '—';
    return parts.join(' - ');
  }

  formatResidualSolvents(solvents: ResidualSolvent[]): string {
    if (!solvents || solvents.length === 0) return '—';
    return solvents.map((s) => s.solvent?.name || '—').join(', ');
  }

  formatSolubility(items: SolubidityInSolvent[]): string {
    if (!items || items.length === 0) return '—';
    return items.map((i) => i.solvent?.name || '—').join(', ');
  }

  formatExternalSupplier(supplier?: ExternalSupplier): string {
    if (!supplier) return '—';
    return `${supplier.supplier?.name || '—'} (${supplier.registryNumber || '—'})`;
  }

  formatPurity(items: PurityCalculation[]): string {
    if (!items || items.length === 0) return '—';
    return items.map((p) => `${p.type}: ${p.purity}`).join(', ');
  }

  formatPurityValue(purity: EnteredValue<string> | undefined): string {
    if (!purity) return '—';
    if (purity.value === undefined || purity.value === null) return '—';
    return `${purity.value}${purity.unit ? ' ' + purity.unit : ''}`.trim();
  }

  formatPrecursorReactantIds(): string {
    const ids = this.reaction().precursorReactantIds;
    if (!ids || ids.length === 0) return '—';
    return ids.map((id) => id.stringForm || '—').join(', ');
  }

  // Chip list helper for melting point (still using chip-list component)
  getMeltingPointChips(): string[] {
    const mp = this.sample().meltingPoint;
    return mp ? [this.formatMeltingPoint(mp)] : [];
  }

  getSolubilityChips(): string[] {
    return this.sample().solubilityInSolvents?.map((i) => i.solvent?.name || '—') || [];
  }
}
