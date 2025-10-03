import { Injectable } from '@angular/core';
import { Ketcher } from 'ketcher-core';
import { Mutation } from '@/core/types/entities/experiments/mutation.i';

export interface KetcherAnalysis {
  rxnData: string;
}

@Injectable({
  providedIn: 'root'
})
export class MutationBuilderService {

  /**
   * Analyzes Ketcher instance and extracts RXN data
   */
  async analyzeKetcher(ketcher: Ketcher): Promise<KetcherAnalysis> {
    try {
      const rxnData = await ketcher.getRxn();

      return {
        rxnData
      };
    } catch (error) {
      console.error('Error analyzing Ketcher data:', error);
      throw new Error('Failed to analyze chemical structure data');
    }
  }

  /**
   * Builds SetScheme mutation from Ketcher data
   */
  async buildMutationsFromKetcher(
    ketcher: Ketcher, 
    reactionAnchor: string
  ): Promise<Mutation[]> {
    // Get RXN data from Ketcher
    const analysis = await this.analyzeKetcher(ketcher);
    
    // For now, only return SetScheme mutation
    const mutations: Mutation[] = [];

    // SetScheme - Main mutation with molFile
    mutations.push({
      type: 'SetScheme',
      anchor: reactionAnchor,
      molFile: analysis.rxnData
    });

    return mutations;
  }
}