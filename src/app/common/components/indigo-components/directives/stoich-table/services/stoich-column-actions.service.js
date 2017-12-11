var StoichRow = require('../domain/stoich-row');

stoichColumnActions.$inject = ['registrationService', 'calculationService', '$q', 'appUnits',
    'dictionaryService', 'sdImportHelper', 'dialogService', 'searchService', 'notifyService'];

function stoichColumnActions(registrationService, calculationService, $q, appUnits, dictionaryService,
                             sdImportHelper, dialogService, searchService, notifyService) {
    return {
        fetchBatchByCompoundId: fetchBatchByCompoundId,
        cleanReactant: cleanReactant,
        cleanReactants: cleanReactants,
        fetchBatchByNbkNumber: fetchBatchByNbkNumber,
        alertWrongFormat: alertWrongFormat,
        populateFetchedBatch: populateFetchedBatch
    };

    function fetchBatchByCompoundId(row, compoundId) {
        var searchRequest = {compoundNo: compoundId};

        return registrationService
            .compounds(searchRequest)
            .$promise
            .then(function(result) {
                return convertCompoundsToBatches(result.slice(0, 20));
            })
            .then(function(batches) {
                return processingBatches(row, compoundId, batches);
            });
    }

    function convertCompoundsToBatches(compounds) {
        return dictionaryService.all()
            .$promise
            .then(function(dicts) {
                return _.map(compounds, function(compound) {
                    return getStoichRow(compound, dicts);
                });
            })
            .then(function(stoichRows) {
                return updateFieldsWhichRelatedToMolecule(stoichRows);
            });
    }

    function populateFetchedBatch(row, source) {
        var isLimiting = row.isLimiting();
        _.extend(row, source);
        row.$$populatedBatch = true;
        row.limiting = isLimiting;
    }

    function cleanReactant(batch) {
        return {
            structure: batch.structure,
            compoundId: batch.compoundId,
            fullNbkBatch: batch.fullNbkBatch,
            molWeight: batch.molWeight,
            formula: batch.formula,
            saltCode: batch.saltCode,
            saltEq: batch.saltEq,
            rxnRole: batch.rxnRole,
            chemicalName: batch.chemicalName
        };
    }

    function cleanReactants(reactants) {
        return _.map(reactants, cleanReactant);
    }

    function fetchBatchByNbkNumber(nbkBatch) {
        var searchRequest = {
            advancedSearch: [{
                condition: 'contains', field: 'fullNbkBatch', name: 'NBK batch #', value: nbkBatch
            }],
            databases: ['Indigo ELN']
        };

        return searchService.search(searchRequest).$promise.then(function(result) {
            return result.slice(0, 5);
        });
    }

    function alertWrongFormat() {
        notifyService.error('Notebook batch number does not exist or in the wrong format- format should be "nbk. ' +
            'number-exp. number-batch number"');
    }

    function getStoichRow(compound, dicts) {
        var json = {
            chemicalName: compound.chemicalName,
            compoundId: compound.compoundNo,
            conversationalBatchNumber: compound.conversationalBatchNo,
            fullNbkBatch: compound.batchNo,
            casNumber: compound.casNo,
            structure: {
                structureType: 'molecule',
                molfile: compound.structure
            },
            formula: compound.formula,
            stereoisomer: sdImportHelper.getWord(
                'Stereoisomer Code',
                'name',
                compound.stereoisomerCode,
                dicts
            ),
            saltCode: _.find(appUnits.saltCodeValues, function(sc) {
                return sc.regValue === compound.saltCode;
            }),
            saltEq: {
                value: compound.saltEquivs, entered: false
            },
            comments: compound.comment
        };

        return StoichRow.fromJson(json);
    }

    function processingBatches(row, compoundId, batches) {
        if (batches.length === 1) {
            populateFetchedBatch(row, batches[0]);
        } else if (batches.length > 1) {
            return dialogService
                .structureValidation(batches, compoundId)
                .then(function(selectedBatch) {
                    populateFetchedBatch(row, selectedBatch);
                });
        } else {
            return $q.reject(batches);
        }

        return batches;
    }

    function updateFieldsWhichRelatedToMolecule(stoichRows) {
        return $q.all(_.map(stoichRows, function(row) {
            return $q.all([
                getMoleculaInfoPromise(row),
                getImagePromise(row)
            ]);
        })).then(function() {
            return stoichRows;
        });

        function getMoleculaInfoPromise(row) {
            return calculationService.getMoleculeInfo(row)
                .then(function(molInfo) {
                    row.formula = molInfo.molecularFormula;
                    row.molWeight.value = molInfo.molecularWeight;
                    row.molWeight.originalValue = molInfo.molecularWeight;
                });
        }

        function getImagePromise(row) {
            return calculationService.getImageForStructure(row.structure.molfile, 'molecule')
                .then(function(image) {
                    row.structure.image = image;
                });
        }
    }
}

module.exports = stoichColumnActions;
