/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var ReagentViewRow = require('../domain/reagent/view-row/reagent-view-row');
var fieldTypes = require('../../../services/calculation/field-types');

/* @ngInject */
function stoichColumnActions(registrationService, calculationService, $q, appValuesService, dictionaryService,
                             sdImportHelper, dialogService, searchService, formatValidation) {
    var commonReactantProperties = [
        fieldTypes.compoundId,
        fieldTypes.formula,
        fieldTypes.fullNbkBatch,
        fieldTypes.molWeight,
        fieldTypes.rxnRole,
        fieldTypes.saltCode,
        fieldTypes.saltEq,
        fieldTypes.structure
    ];

    return {
        fetchBatchByCompoundId: fetchBatchByCompoundId,
        cleanReactant: cleanReactant,
        cleanReactants: cleanReactants,
        fetchBatchByNbkNumber: fetchBatchByNbkNumber,
        getRowFromNbkBatch: getRowFromNbkBatch
    };

    function fetchBatchByCompoundId(row, compoundId) {
        // Validate compoundId format
        var isIdValid = validateCompoundId(compoundId);

        // If its incorrect - abort searching
        if (!isIdValid) {
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
        _.assign(originalRow, newRow);
        originalRow.$$populatedBatch = true;
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

    function cleanReactant(batch) {
        // Collect required properties
        var properties = commonReactantProperties.concat([
            fieldTypes.chemicalName
        ]);

        // Extracting required properties from given batch object
        return _.assign({}, _.pick(batch, properties));
    }

    function getRowFromNbkBatch(row, batchObj) {
        // Collect required properties
        var properties = commonReactantProperties.concat([
            fieldTypes.density,
            fieldTypes.eq,
            fieldTypes.fullNbkImmutablePart,
            fieldTypes.loadFactor,
            fieldTypes.mol,
            fieldTypes.molarity,
            fieldTypes.stoicPurity,
            fieldTypes.structureComments,
            fieldTypes.volume,
            fieldTypes.weight
        ]);

        // And create new table row with them
        var newRow = new ReagentViewRow(_.pick(batchObj, properties));

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
            saltCode: _.find(appValuesService.getSaltCodeValues(), function(sc) {
                return sc.regValue === compound.saltCode;
            }),
            saltEq: {
                value: compound.saltEquivs, entered: false
            },
            comments: compound.comment
        };

        return new ReagentViewRow(rowProps);
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
        var isValid = formatValidation.indigoCompoundId.test(compoundId)
            || formatValidation.indigoCompoundIdFull.test(compoundId);

        return isValid;
    }
}

module.exports = stoichColumnActions;
