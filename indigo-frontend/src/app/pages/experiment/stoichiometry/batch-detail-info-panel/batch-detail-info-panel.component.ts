import { Component, computed, inject, input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CdkAccordionModule } from '@angular/cdk/accordion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { Reaction, ReactionOutput, ReactionOutputSample } from '@core/types/entities/experiments/experiment.i';
import { 
  MeltingPoint, 
  ExternalSupplier, 
  PurityCalculation, 
  ResidualSolvent, 
  SolubidityInSolvent
} from '@core/types/entities/experiments/experiment-shared.i';
import { BuiltInDictionaryService } from '@core/services/health-hazards/built-in-dictionary.service';
import { BuiltInDictionary, DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { ChipListComponent } from '../shared/chip-list/chip-list.component';

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
    FormsModule,
    ChipListComponent,
  ],
  standalone: true,
})
export class BatchDetailInfoPanelComponent implements OnInit {
  private dictionaryService = inject(BuiltInDictionaryService);

  sample = input.required<ReactionOutputSample>();
  output = input.required<ReactionOutput>();
  reaction = input.required<Reaction>();

  // Dictionary data - single computed returning Map of all dictionaries
  readonly dicts = computed(() => 
    this.dictionaryService.getDictionaryItems([
      BuiltInDictionary.SAMPLE_SOURCE,
      BuiltInDictionary.SAMPLE_SOURCE_DETAILS,
      BuiltInDictionary.STEREOISOMER_CODE,
      BuiltInDictionary.COMPONENT_STATE,
      BuiltInDictionary.COMPOUND_PROTECTION,
      BuiltInDictionary.HEALTH_HAZARD,
      BuiltInDictionary.HANDLING_PRECAUTIONS,
      BuiltInDictionary.STORAGE_INSTRUCTIONS,
      BuiltInDictionary.SOLVENT,
      BuiltInDictionary.EXTERNAL_SUPPLIER,
    ])
  );

  // Expose enum for template access
  readonly BuiltInDictionary = BuiltInDictionary;

  // Additional Information accordion state
  additionalInfoExpanded = false;

  ngOnInit(): void {
    // Load all required dictionaries
    this.dictionaryService.load([
      BuiltInDictionary.SAMPLE_SOURCE,
      BuiltInDictionary.SAMPLE_SOURCE_DETAILS,
      BuiltInDictionary.STEREOISOMER_CODE,
      BuiltInDictionary.COMPONENT_STATE,
      BuiltInDictionary.COMPOUND_PROTECTION,
      BuiltInDictionary.HEALTH_HAZARD,
      BuiltInDictionary.HANDLING_PRECAUTIONS,
      BuiltInDictionary.STORAGE_INSTRUCTIONS,
      BuiltInDictionary.SOLVENT,
      BuiltInDictionary.EXTERNAL_SUPPLIER,
    ]);
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
    return solvents.map(s => s.solvent?.name || '—').join(', ');
  }

  formatHealthHazards(hazards: DictionaryItemRef[]): string {
    if (!hazards || hazards.length === 0) return '—';
    return hazards.map(h => h.name).join(', ');
  }

  formatSolubility(items: SolubidityInSolvent[]): string {
    if (!items || items.length === 0) return '—';
    return items.map(i => i.solvent?.name || '—').join(', ');
  }

  formatHandlingPrecautions(items: DictionaryItemRef[]): string {
    if (!items || items.length === 0) return '—';
    return items.map(i => i.name).join(', ');
  }

  formatStorageInstructions(items: DictionaryItemRef[]): string {
    if (!items || items.length === 0) return '—';
    return items.map(i => i.name).join(', ');
  }

  formatExternalSupplier(supplier?: ExternalSupplier): string {
    if (!supplier) return '—';
    return `${supplier.supplier?.name || '—'} (${supplier.registryNumber || '—'})`;
  }

  formatPurity(items: PurityCalculation[]): string {
    if (!items || items.length === 0) return '—';
    return items.map(p => `${p.type}: ${p.purity}`).join(', ');
  }

  formatPrecursorReactantIds(): string {
    const ids = this.reaction().precursorReactantIds;
    if (!ids || ids.length === 0) return '—';
    return ids.map(id => id.stringForm || '—').join(', ');
  }

  formatCompoundProtection(items: DictionaryItemRef[]): string {
    if (!items || items.length === 0) return '—';
    return items.map(cp => cp.name).join(', ');
  }

  // Chip list helpers - transform arrays to string[] for chip-list component
  getMeltingPointChips(): string[] {
    const mp = this.sample().meltingPoint;
    return mp ? [this.formatMeltingPoint(mp)] : [];
  }

  getStorageInstructionsChips(): string[] {
    return this.sample().storageInstructions?.map(i => i.name) || [];
  }

  getHealthHazardsChips(): string[] {
    return this.sample().healthHazards?.map(i => i.name) || [];
  }

  getSolubilityChips(): string[] {
    return this.sample().solubilityInSolvents?.map(i => i.solvent?.name || '—') || [];
  }

  getHandlingPrecautionsChips(): string[] {
    return this.sample().handlingPrecautions?.map(i => i.name) || [];
  }
}
