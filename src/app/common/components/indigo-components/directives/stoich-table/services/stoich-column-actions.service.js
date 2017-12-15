var StoichRow = require('../domain/stoich-row');

/* @ngInject */
function stoichColumnActions(registrationService, calculationService, $q, appUnits, dictionaryService,
                             sdImportHelper, dialogService, searchService) {
    return {
        fetchBatchByCompoundId: fetchBatchByCompoundId,
        cleanReactant: cleanReactant,
        cleanReactants: cleanReactants,
        fetchBatchByNbkNumber: fetchBatchByNbkNumber,
        getRowFromNbkBatch: getRowFromNbkBatch
    };

    function fetchBatchByCompoundId(row, compoundId) {
        // Validate compoundId format
        var idIsValid = validateCompoundId(compoundId);

        // If its incorrect - abort searching
        if (!idIsValid) {
            return $q.reject('Incorrect format');
        }

        // Perform search otherwise
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

    function populateFetchedBatch(originalRow, newRow) {
        // Updates original table row with the one generated from batch or
        // fetched by compoundId
        var isLimiting = originalRow.isLimiting();

        _.extend(originalRow, newRow);
        originalRow.$$populatedBatch = true;
        originalRow.limiting = isLimiting;
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

    function fetchBatchByNbkNumber(batchNumber) {
        var searchRequest = {
            advancedSearch: [{
                condition: 'contains',
                field: 'fullNbkBatch',
                name: 'NBK batch #',
                value: batchNumber
            }],
            databases: ['Indigo ELN']
        };

        return searchService.search(searchRequest).$promise
            .then(function(result) {
                if (!_.isArray(result) || _.isEmpty(result)) {
                    return $q.reject('Nothing found');
                }

                return result.slice(0, 5);
            });
    }

    function getRowFromNbkBatch(row, batchObj) {
        // Select some properies from fetched batch object
        // And create new table row with them
        var propertiesToPick = [
            'compoundId',
            'density',
            'eq',
            'formula',
            'fullNbkBatch',
            'fullNbkImmutablePart',
            'loadFactor',
            'mol',
            'molWeight',
            'molarity',
            'rxnRole',
            'saltCode',
            'saltEq',
            'stoicPurity',
            'structure',
            'structureComments',
            'volume',
            'weight'
        ];

        var newRow = new StoichRow(_.pick(batchObj, propertiesToPick));

        populateFetchedBatch(row, newRow);
    }

    function getStoichRow(compound, dicts) {
        var rowProps = {
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

        return new StoichRow(rowProps);
    }

    function processingBatches(row, compoundId, batches) {
        if (_.isEmpty(batches)) {
            return $q.reject(batches);
        }

        if (batches.length === 1) {
            populateFetchedBatch(row, batches[0]);
        } else {
            return dialogService
                .structureValidation(batches, compoundId)
                .then(function(selectedBatch) {
                    populateFetchedBatch(row, selectedBatch);
                });
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

    function validateCompoundId(compoundId) {
        // Regex for cmpoundId validation
        // Matches 'STR-00000012'
        var indigoCompoundIdFormat = /^STR-\d{8}$/;
        // Matches 'STR-00000012-01'
        var indigoCompoundIdFormatFull = /^STR-\d{8}-\d{2}$/;

        return indigoCompoundIdFormat.test(compoundId) || indigoCompoundIdFormatFull.test(compoundId);
    }
}

module.exports = stoichColumnActions;
