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
            onChanged: '&'
        }
    };
}

/* @ngInject */
function IndigoStoichTableController($scope, $rootScope, $q, $uibModal, appValuesService, stoichColumnActions,
                                     alertModal, notifyService, calculationService, stoichTableCache,
                                     stoichReactantsColumns, stoichProductColumns, stoichTableHelper, i18en) {
    var vm = this;

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

        // TODO: refactor

        // Handle value change
        if (changedField === stoichReactantsColumns.fullNbkBatch.id) {
            // Update row based on selected notebook batch number
            onNbkBatchNumberChanged(changedRow);
        }

        if (changedField === stoichReactantsColumns.compoundId.id) {
            // Update row based on selected compoundId
            onCompoundIdChanged(changedRow, changedRow[changedField])
                .then(function() {
                    stoichTableHelper.onReagentsChanged({
                        rows: rows,
                        changedRow: changedRow,
                        changedField: changedField
                    });
                });

            return;
        }

        stoichTableHelper.onReagentsChanged({
            rows: rows,
            changedRow: changedRow,
            changedField: changedField
        });
    }

    function onProductsChanged(change) {
        var tableRow = change.row;
        var tableColumnId = change.column;
    }

    function removeRow() {
        _.remove(vm.componentData.reactants, vm.selectedRow);
        vm.selectedRow = null;
        updateReactants();
        // calculationService.recalculateStoich(vm.componentData);
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
                    // TODO: Use translate service
                    alertModal.info(i18en.NOTIFY_STOICHIOMETRY_SYNCHRONISED, 'sm');

                    return;
                }

                $uibModal.open({
                    animation: true,
                    size: 'lg',
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
                                // checkLimiting();
                            };
                        }
                    }
                });
            });
    }

    function checkLimiting() {
        var limiting = vm.limitingRow || calculationService.findLimiting(vm.componentData.reactants);
        if (!limiting && vm.componentData.reactants.length) {
            vm.limitingRow = _.first(vm.componentData.reactants);
            vm.limitingRow.limiting = true;
        }
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
        $uibModal.open({
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
            stoichProductColumns.weight,
            stoichProductColumns.mol,
            stoichProductColumns.saltCode,
            stoichProductColumns.saltEq,
            stoichProductColumns.hazardComments,
            stoichProductColumns.eq
        ];
    }

    /* eslint no-unused-vars: "off"*/
    function onCloseCell(column, data) {

        // if (_.includes(columnsWithClose, column.id)) {
        //     if (column.id === stoichReactantsColumns.compoundId.id) {
        //         onCloseCompoundId(data);
        //     }
        //     calculationService.setEntered(data);
        //     if (column.id === stoichReactantsColumns.rxnRole.id) {
        //         onRxnRoleChange(data);
        //         updatePrecursors();
        //     }
        //     calculationService.recalculateStoichBasedOnBatch(data).then(updateReactantsAndProducts);
        // } else if (column.id === stoichReactantsColumns.fullNbkBatch.id) {
        //     stoichColumnActions.onCloseFullNbkBatch(data).then(updatePrecursors);
        // }
    }

    function updatePrecursors() {
        var precursors = stoichTableHelper.getPrecursors(vm.componentData.reactants);
        vm.onPrecursorsChanged({precursors: precursors});
    }

    function getLimitingColumn() {
        return _.extend({}, stoichReactantsColumns.limiting, {
            onClick: function(data) {
                calculationService.setEntered(data);
                calculationService.recalculateStoichBasedOnBatch(data).then(updateReactantsAndProducts);
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

        // TODO: Use translate service
        notifyService.error(i18en.NOTIFY_COMPOUND_ERROR);
    }

    function alertWrongButchNumberFormat() {
        // TODO: Use translate service
        notifyService.error(i18en.NOTIFY_BATCH_NUMBER_ERROR);
    }

    function updateReactants() {
        // checkLimiting();
        updatePrecursors();
    }

    function addReagentRow(reagentRow) {
        vm.componentData.reactants.push(reagentRow);

        var change = {
            rows: vm.componentData.reactants,
            changedRow: reagentRow,
            changedField: null
        };

        stoichTableHelper.onReagentsChanged(change);

        vm.onChangedReactants({reactants: vm.componentData.reactants});
        vm.onChanged();
    }

    function getStructureImagesForIntendedProducts() {
        _.forEach(vm.componentData.products, function(item) {
            if (!item.structure.image) {
                calculationService.getImageForStructure(item.structure.molfile, 'molecule', function(image) {
                    item.structure.image = image;
                });
            }
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
            vm.componentData.products = vm.infoProducts;
            getStructureImagesForIntendedProducts();
        }

        calculationService.recalculateStoich(vm.componentData);
    }

    function bindEvents() {
        $scope.$watch('vm.infoProducts', getReactionProductsAndReactants, true);

        $scope.$watch('vm.componentData', function(table) {
            stoichTableCache.setStoicTable(table);
        });

        $scope.$watch('vm.componentData.products', function(products) {
            _.forEach(products, function(batch) {
                if (!batch.$$batchHash) {
                    batch.$$batchHash = batch.formula + batch.exactMass;
                }
            });
            vm.onChangedProducts({products: products});
        }, true);
    }

    function updateReactantsAndProducts(data) {
        var newReactants = data.stoicBatches;
        var newProducts = data.intendedProducts;
        if (vm.componentData.reactants && newReactants.length === vm.componentData.reactants.length) {
            _.forEach(vm.componentData.reactants, function(reactant, i) {
                _.extend(reactant, newReactants[i]);
            });
        }
        if (vm.componentData.products && newProducts.length === vm.componentData.products.length) {
            _.forEach(vm.componentData.products, function(product, i) {
                _.extend(product, newProducts[i]);
            });
        }
    }

    function isReactantAlreadyInStoic(responces) {
        return _.some(responces);
    }

    function findLikedReactant(reactant) {
        return !!_.find(vm.componentData.reactants, function(infoReactant) {
            return infoReactant.formula === reactant.formula;
        });
    }

    function getMissingReactionReactantsInStoic(reactantsInfo) {
        var reactantsToSearch = [];
        var stoicReactants = stoichTableHelper.getReactantsWithMolfile(vm.componentData.reactants);

        if (_.isEmpty(reactantsInfo) || stoicReactants.length !== reactantsInfo.length) {
            _.forEach(reactantsInfo, function(reactant) {
                if (!findLikedReactant(reactant) && stoichTableHelper.isReactantWithMolfile(reactant)) {
                    reactantsToSearch.push(reactant);
                }
            });

            return $q.resolve(reactantsToSearch);
        }

        var allPromises = _.map(reactantsInfo, function(reactantInfo, index) {
            var reactantsEqualityPromises = _.map(stoicReactants, function(stoicReactant) {
                return calculationService.isMoleculesEqual(
                    stoicReactant.structure.molfile,
                    reactantInfo.structure.molfile
                );
            });

            return $q.all(reactantsEqualityPromises).then(function(responces) {
                if (!isReactantAlreadyInStoic(responces)) {
                    reactantsToSearch[index] = reactantInfo;
                }
            });
        });

        return $q.all(allPromises).then(function() {
            return reactantsToSearch;
        });
    }
}

module.exports = indigoStoichTable;
