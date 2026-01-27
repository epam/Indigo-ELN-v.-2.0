import { Component, computed, inject, input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CdkAccordionModule } from '@angular/cdk/accordion';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { ReactionOutput, ReactionOutputSample } from '@core/types/entities/experiments/experiment.i';
import { 
  MeltingPoint, 
  ExternalSupplier, 
  PurityCalculation, 
  ResidualSolvent, 
  SolubidityInSolvent,
  STRCodeCompound
} from '@core/types/entities/experiments/experiment-shared.i';
import { BuiltInDictionaryService } from '@core/services/health-hazards/built-in-dictionary.service';
import { BuiltInDictionary, DictionaryItemRef } from '@core/types/entities/dictionary.i';

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
  ],
  standalone: true,
})
export class BatchDetailInfoPanelComponent implements OnInit {
  private dictionaryService = inject(BuiltInDictionaryService);

  sample = input.required<ReactionOutputSample>();
  output = input.required<ReactionOutput>();

  // Dictionary data
  sourceDictionary = computed(() => 
    this.dictionaryService.getDictionaryItems(BuiltInDictionary.SAMPLE_SOURCE));
  sourceDetailsDictionary = computed(() => 
    this.dictionaryService.getDictionaryItems(BuiltInDictionary.SAMPLE_SOURCE_DETAILS));
  stereoisomerDictionary = computed(() => 
    this.dictionaryService.getDictionaryItems(BuiltInDictionary.STEREOISOMER_CODE));
  componentStateDictionary = computed(() => 
    this.dictionaryService.getDictionaryItems(BuiltInDictionary.COMPONENT_STATE));
  compoundProtectionDictionary = computed(() => 
    this.dictionaryService.getDictionaryItems(BuiltInDictionary.COMPOUND_PROTECTION));
  healthHazardsDictionary = computed(() => 
    this.dictionaryService.getDictionaryItems(BuiltInDictionary.HEALTH_HAZARD));
  handlingPrecautionsDictionary = computed(() => 
    this.dictionaryService.getDictionaryItems(BuiltInDictionary.HANDLING_PRECAUTIONS));
  storageInstructionsDictionary = computed(() => 
    this.dictionaryService.getDictionaryItems(BuiltInDictionary.STORAGE_INSTRUCTIONS));
  solventDictionary = computed(() => 
    this.dictionaryService.getDictionaryItems(BuiltInDictionary.SOLVENT));
  externalSupplierDictionary = computed(() => 
    this.dictionaryService.getDictionaryItems(BuiltInDictionary.EXTERNAL_SUPPLIER));

  // Additional Information accordion state
  additionalInfoExpanded = false;

  ngOnInit(): void {
    // Load all required dictionaries
    this.dictionaryService.load(BuiltInDictionary.SAMPLE_SOURCE);
    this.dictionaryService.load(BuiltInDictionary.SAMPLE_SOURCE_DETAILS);
    this.dictionaryService.load(BuiltInDictionary.STEREOISOMER_CODE);
    this.dictionaryService.load(BuiltInDictionary.COMPONENT_STATE);
    this.dictionaryService.load(BuiltInDictionary.COMPOUND_PROTECTION);
    this.dictionaryService.load(BuiltInDictionary.HEALTH_HAZARD);
    this.dictionaryService.load(BuiltInDictionary.HANDLING_PRECAUTIONS);
    this.dictionaryService.load(BuiltInDictionary.STORAGE_INSTRUCTIONS);
    this.dictionaryService.load(BuiltInDictionary.SOLVENT);
    this.dictionaryService.load(BuiltInDictionary.EXTERNAL_SUPPLIER);
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

  formatPrecursorReactantIds(ids: STRCodeCompound[]): string {
    if (!ids || ids.length === 0) return '—';
    return ids.map(id => id.stringForm || '—').join(', ');
  }

  formatCompoundProtection(items: DictionaryItemRef[]): string {
    if (!items || items.length === 0) return '—';
    return items.map(cp => cp.name).join(', ');
  }
}
