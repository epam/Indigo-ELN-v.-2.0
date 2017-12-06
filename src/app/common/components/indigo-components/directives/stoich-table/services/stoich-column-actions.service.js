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
        populateFetchedBatch: populateFetchedBatch,
        onCloseFullNbkBatch: onCloseFullNbkBatch
    };

    function onCloseFullNbkBatch(data) {
        var row = data.row;
        var nbkBatch = data.model;
        if (!row.$$populatedBatch) {
            if (row.fullNbkBatch) {
                return fetchBatchByNbkNumber(nbkBatch).then(function(result) {
                    var pb = result[0];
                    if (pb && pb.details.fullNbkBatch === row.fullNbkBatch) {
                        pb.details.compoundId = row.compoundId;
                        populateFetchedBatch(row, pb.details);
                    } else {
                        alertWrongFormat();
                        row.fullNbkBatch = row.$$fullNbkBatchOld;
                    }
                });
            }
            alertWrongFormat();
            row.fullNbkBatch = row.$$fullNbkBatchOld;
        }

        return $q.resolve();
    }

    function fetchBatchByCompoundId(compoundId, row) {
        var searchRequest = {compoundNo: compoundId};

        return registrationService
            .compounds(searchRequest)
            .$promise
            .then(function(result) {
                return convertCompoundsToBatches(result.slice(0, 20)).then(function(batches) {
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
                });
            });
    }

    function convertCompoundsToBatches(compounds) {
        return dictionaryService.all().$promise
            .then(function(dicts) {
                return _.map(compounds, function(c) {
                    return {
                        chemicalName: c.chemicalName,
                        compoundId: c.compoundNo,
                        conversationalBatchNumber: c.conversationalBatchNo,
                        fullNbkBatch: c.batchNo,
                        casNumber: c.casNo,
                        structure: {
                            structureType: 'molecule',
                            molfile: c.structure
                        },
                        formula: c.formula,
                        stereoisomer: sdImportHelper.getWord(
                            'Stereoisomer Code',
                            'name',
                            c.stereoisomerCode,
                            dicts
                        ),
                        saltCode: _.find(appUnits.saltCodeValues, function(sc) {
                            return sc.regValue === c.saltCode;
                        }),
                        saltEq: {
                            value: c.saltEquivs, entered: false
                        },
                        comments: c.comment
                    };
                });
            })
            .then(function(result) {
                return $q.all(_.map(result, function(item) {
                    return $q.all([
                        calculationService.getMoleculeInfo(item).then(function(molInfo) {
                            item.formula = molInfo.molecularFormula;
                            item.molWeight = item.molWeight || {};
                            item.molWeight.value = molInfo.molecularWeight;
                        }),
                        calculationService.getImageForStructure(item.structure.molfile, 'molecule')
                            .then(function(image) {
                                item.structure.image = image;
                            })
                    ]);
                })).then(function() {
                    return result;
                });
            });
    }

    function populateFetchedBatch(row, source) {
        var cleanedBatch = cleanReactant(source);
        row.$$populatedBatch = true;
        _.extend(row, cleanedBatch);
        row.rxnRole = row.rxnRole || angular.copy(appUnits.rxnRoleReactant);
        row.weight = null;
        row.volume = null;
        calculationService.recalculateStoich();
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
}

module.exports = stoichColumnActions;
