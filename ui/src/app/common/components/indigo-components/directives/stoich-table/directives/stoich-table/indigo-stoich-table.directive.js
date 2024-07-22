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

var template = require('./indigo-stoich-table.html');
var analyzeRxnTemplate = require('../../../../common/analyze-rxn/analyze-rxn.html');
var searchReagentsTemplate = require('../../../../common/search-reagents/search-reagents.html');
var ReagentViewRow = require('../../domain/reagent/view-row/reagent-view-row');

function indigoStoichTable() {
    return {
        restrict: 'E',
        template: template,
        controller: IndigoStoichTableController,
        controllerAs: 'vm',
        bindToController: true,
        scope: {
            componentData: '=',
            experiment: '=',
            isReadonly: '=',
            infoReactants: '=',
            infoProducts: '=',
            onChangedReactants: '&',
            onChangedProducts: '&',
            onPrecursorsChanged: '&',
            onStoichTableChanged: '&',
            onChanged: '&'
        }
    };
}

/* @ngInject */
function IndigoStoichTableController($scope, $rootScope, $q, $uibModal, appValuesService, stoichColumnActions,
                                     alertModal, notifyService, calculationService, stoichTableCache,
                                     stoichReactantsColumns, stoichProductColumns, stoichTableHelper, translateService) {
    var vm = this;

    var dlg;

    init();

    function init() {
        vm.reactantsColumns = getReactantColumns();
        vm.productsColumns = getProductColumns();
        vm.limitingRow = null;

        vm.clear = clear;
        vm.appendRow = appendRow;
        vm.removeRow = removeRow;
        vm.onRowSelected = onRowSelected;
        vm.noReactantsInStoic = noReactantsInStoic;
        vm.analyzeRxn = analyzeRxn;
        vm.createRxn = createRxn;
        vm.searchReagents = searchReagents;
        vm.onReagentsChanged = onReagentsChanged;
        vm.onProductsChanged = onProductsChanged;

        bindEvents();
    }

    function clear() {
        vm.selectedRow.clear();
        vm.onChanged();
    }

    function appendRow() {
        var reagentRow = new ReagentViewRow();
        addReagentRow(reagentRow);
    }

    function onReagentsChanged(change) {
        var rows = vm.componentData.reactants;
        var changedRow = change.row;
        var changedField = change.column;

        if (_.isObject(changedRow[changedField])
            && changedRow[changedField].value && changedRow[changedField].value === change.oldVal) {
            return;
        }

        switch (changedField) {
            case stoichReactantsColumns.fullNbkBatch.id:
                // Update row based on selected notebook batch number
                onNbkBatchNumberChanged(changedRow);
                recalculateStoichTable(rows, changedRow, changedField);
                break;
            case stoichReactantsColumns.rxnRole.id:
                updatePrecursors();
                recalculateStoichTable(rows, changedRow, changedField);
                break;
            case stoichReactantsColumns.compoundId.id:
                // Update row based on selected compoundId
                onCompoundIdChanged(changedRow, changedRow[changedField])
                    .then(function() {
                        recalculateStoichTable(rows, changedRow, changedField);
                    });
                break;
            default:
                recalculateStoichTable(rows, changedRow, changedField);
                break;
        }
    }

    function onProductsChanged(change) {
        var rows = vm.componentData.products;
        var idOfChangedRow = change ? change.row.id : null;
        var changedField = change ? change.column : null;
        var limitingRow = stoichTableHelper.findLimitingRow(vm.componentData.reactants);

        stoichTableHelper.onProductsChanged({
            rows: rows,
            idOfChangedRow: idOfChangedRow,
            changedField: changedField,
            limitingRow: limitingRow
        });
    }

    function recalculateStoichTable(rows, changedRow, changedField) {
        stoichTableHelper.onReagentsChanged({
            rows: rows,
            idOfChangedRow: changedRow.id,
            changedField: changedField
        });

        onProductsChanged();
        updateBatches();
    }

    function updateBatches() {
        vm.onStoichTableChanged({stoichTable: vm.componentData});
    }

    function removeRow() {
        _.remove(vm.componentData.reactants, vm.selectedRow);
        vm.selectedRow = null;
        updatePrecursors();
        onProductsChanged();
        vm.onChanged();
    }

    function onRowSelected(row) {
        vm.selectedRow = row || null;
    }

    function noReactantsInStoic() {
        return stoichTableHelper.getReactantsWithMolfile(vm.componentData.reactants).length === 0;
    }

    function analyzeRxn() {
        getMissingReactionReactantsInStoic(vm.infoReactants)
            .then(function(reactantsToSearch) {
                if (_.isEmpty(reactantsToSearch)) {
                    alertModal.info(translateService.translate('NOTIFY_STOICHIOMETRY_SYNCHRONISED'), 'sm');

                    return;
                }

                closeDialog();
                dlg = $uibModal.open({
                    animation: true,
                    size: 'lg',
                    backdrop: 'static',
                    controller: 'AnalyzeRxnController',
                    controllerAs: 'vm',
                    template: analyzeRxnTemplate,
                    resolve: {
                        reactants: function() {
                            return reactantsToSearch;
                        },
                        addTableRowsCallback: function() {
                            return function(reactants) {
                                // Inserting new rows into table
                                _.forEach(reactants, function(reactant) {
                                    var row = new ReagentViewRow(reactant);
                                    addReagentRow(row);
                                });
                            };
                        }
                    }
                });
            });
    }

    function createRxn() {
        var REACTANT = appValuesService.getRxnRoleReactant().name;
        var stoicReactantsMolfiles = _.compact(_.map(vm.componentData.reactants, function(batch) {
            return batch.rxnRole.name === REACTANT && batch.structure.molfile;
        }));
        var intendedProductsMolfiles = _.compact(_.map(vm.componentData.products, function(batch) {
            return batch.structure.molfile;
        }));
        calculationService.combineReactionComponents(stoicReactantsMolfiles, intendedProductsMolfiles)
            .then(function(result) {
                $rootScope.$broadcast('new-reaction-scheme', result.data);
            });
    }

    function searchReagents(activeTabIndex) {
        closeDialog();
        dlg = $uibModal.open({
            animation: true,
            size: 'lg',
            controller: 'SearchReagentsController',
            controllerAs: 'vm',
            template: searchReagentsTemplate,
            resolve: {
                activeTabIndex: function() {
                    return activeTabIndex;
                },
                addTableRowsCallback: function() {
                    return function(reactants) {
                        // Inserting new rows into table
                        _.forEach(reactants, function(item) {
                            var row = new ReagentViewRow(item);
                            addReagentRow(row);
                        });
                        updatePrecursors();
                    };
                }
            }
        });
    }

    function getReactantColumns() {
        return [
            stoichReactantsColumns.compoundId,
            stoichReactantsColumns.casNumber,
            stoichReactantsColumns.chemicalName,
            stoichReactantsColumns.fullNbkBatch,
            stoichReactantsColumns.molWeight,
            stoichReactantsColumns.weight,
            stoichReactantsColumns.volume,
            stoichReactantsColumns.mol,
            stoichReactantsColumns.eq,
            getLimitingColumn(),
            stoichReactantsColumns.rxnRole,
            stoichReactantsColumns.density,
            stoichReactantsColumns.molarity,
            stoichReactantsColumns.stoicPurity,
            stoichReactantsColumns.formula,
            stoichReactantsColumns.saltCode,
            stoichReactantsColumns.saltEq,
            stoichReactantsColumns.loadFactor,
            stoichReactantsColumns.hazardComments,
            stoichReactantsColumns.comments
        ];
    }

    function getProductColumns() {
        return [
            stoichProductColumns.chemicalName,
            stoichProductColumns.formula,
            stoichProductColumns.molWeight,
            stoichProductColumns.exactMass,
            stoichProductColumns.theoWeight,
            stoichProductColumns.theoMoles,
            stoichProductColumns.saltCode,
            stoichProductColumns.saltEq,
            stoichProductColumns.hazardComments,
            stoichProductColumns.eq
        ];
    }

    function updatePrecursors() {
        var precursors = stoichTableHelper.getPrecursors(vm.componentData.reactants);
        vm.onPrecursorsChanged({precursors: precursors});
    }

    function getLimitingColumn() {
        return _.extend({}, stoichReactantsColumns.limiting, {
            onClick: function() {
                onProductsChanged();
                updateBatches();
            }
        });
    }

    function onNbkBatchNumberChanged(row) {
        // If nothing has been selected, revert changes
        if (!row.fullNbkBatch || _.isEmpty(row.changesQueue)) {
            alertWrongButchNumberFormat();
            row.fullNbkBatch = row.$$fullNbkBatchOld;

            return;
        }

        // Update table row
        var changes = row.changesQueue.pop().details;

        stoichColumnActions.getRowFromNbkBatch(row, changes);
        updatePrecursors();
    }

    function onCompoundIdChanged(row, compoundId) {
        if (!row || !compoundId) {
            return;
        }

        return stoichColumnActions.fetchBatchByCompoundId(row, compoundId)
            .catch(alertCompoundWrongFormat)
            .finally(updatePrecursors);
    }

    function alertCompoundWrongFormat(event) {
        if (event === 'cancel') {
            return;
        }

        notifyService.error(translateService.translate('NOTIFY_COMPOUND_ERROR'));
    }

    function alertWrongButchNumberFormat() {
        notifyService.error(translateService.translate('NOTIFY_BATCH_NUMBER_ERROR'));
    }

    function addReagentRow(reagentRow) {
        vm.componentData.reactants.push(reagentRow);

        recalculateStoichTable(vm.componentData.reactants, reagentRow, null);

        vm.onChangedReactants({reactants: vm.componentData.reactants});
        vm.onChanged();
    }

    function getIntendedProductsWithStructureImages(products) {
        return _.map(products, function(item) {
            if (!item.structure.image) {
                calculationService.getImageForStructure(item.structure.molfile, 'molecule', function(image) {
                    item.structure.image = image;
                });
            }

            return item;
        });
    }

    function isEqualsMolfiles(reaction) {
        return _.some(reaction, function(reagent, index) {
            return _.get(reagent.structure, 'molfile')
                === _.get(vm.componentData, 'products[' + index + '].structure.molfile');
        });
    }

    function getReactionProductsAndReactants(reaction) {
        if (!reaction || isEqualsMolfiles(reaction)) {
            return;
        }

        if (vm.infoProducts && vm.infoProducts.length) {
            var products = getIntendedProductsWithStructureImages(vm.infoProducts);
            vm.componentData.products = stoichTableHelper.convertToProductViewRow(products);
        } else {
            vm.componentData.products.length = 0;
        }
        onProductsChanged();
    }

    function bindEvents() {
        $scope.$watch('vm.infoProducts', getReactionProductsAndReactants, true);

        $scope.$watch('vm.componentData', function(table) {
            stoichTableCache.setStoicTable(table);
        });

        $scope.$watch('vm.componentData.products', function(products) {
            _.forEach(products, function(batch) {
                if (!batch.$$batchHash) {
                    batch.$$batchHash = batch.formula.value + batch.exactMass;
                }
            });
            vm.onChangedProducts({products: products});
        }, true);

        $scope.$on('$destroy', function() {
            closeDialog();
        });
    }

    function getMissingReactionReactantsInStoic(reactantsInfo) {
        var reactantsToSearch = [];
        var stoicReactants = stoichTableHelper.getReactantsWithMolfile(vm.componentData.reactants);

        if (!stoicReactants.length) {
            return $q.resolve(reactantsInfo);
        }

        var stoicReactantsMolfiles = _.map(stoicReactants, function(stoicReactant) {
            return stoicReactant.structure.molfile;
        });

        var allPromises = _.map(reactantsInfo, function(reactantInfo) {
            return calculationService.isMoleculesSubstructure(reactantInfo.structure.molfile, stoicReactantsMolfiles)
                .then(function(equalResults) {
                    if (!equalResults) {
                        reactantsToSearch.push(reactantInfo);
                    }
                });
        });

        return $q.all(allPromises).then(function() {
            return reactantsToSearch;
        });
    }

    function closeDialog() {
        if (dlg) {
            dlg.dismiss();
            dlg = null;
        }
    }
}

module.exports = indigoStoichTable;
