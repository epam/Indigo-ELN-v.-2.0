(function() {
    angular
        .module('indigoeln.Components')
        .controller('IndigoStoichTableController', IndigoStoichTableController);

    IndigoStoichTableController.$inject = [
        '$scope', '$rootScope', '$q', '$uibModal', 'AppValues', 'stoichColumnActions', 'AlertModal', 'notifyService',
        'CalculationService', 'StoichTableCache', 'stoichReactantsColumns', 'stoichProductColumns'
    ];

    function IndigoStoichTableController($scope, $rootScope, $q, $uibModal, AppValues, stoichColumnActions,
                                         AlertModal, notifyService, CalculationService, StoichTableCache,
                                         stoichReactantsColumns, stoichProductColumns) {
        var vm = this;

        var columnsWithClose = [
            'molWeight',
            'weight',
            'volume',
            'mol',
            'eq',
            'density',
            'molarity',
            'stoicPurity',
            'loadFactor',
            'rxnRole'
        ];

        vm.model = vm.model || {};

        vm.clear = clear;
        vm.appendRow = appendRow;
        vm.removeRow = removeRow;
        vm.onRowSelected = onRowSelected;
        vm.noReactantsInStoic = noReactantsInStoic;
        vm.analyzeRxn = analyzeRxn;
        vm.createRxn = createRxn;
        vm.searchReagents = searchReagents;
        vm.onCloseCell = onCloseCell;

        init();

        function clear() {
            _.remove(vm.selectedRow, function(value, key) {
                return !_.includes(['$$hashKey', 'selected'], key);
            });
            vm.selectedRow.rxnRole = AppValues.getRxnRoleReactant();
            vm.onChanged();
        }

        function appendRow() {
            var reactant = CalculationService.createBatch(getStoicTable());
            addStoicReactant(reactant);
        }

        function removeRow() {
            setStoicReactants(_.without(getStoicReactants(), vm.selectedRow));
            updateReactants(vm.model.stoichTable.reactants);
            vm.onChanged();
        }

        function onRowSelected(row) {
            vm.selectedRow = row || null;
        }

        function noReactantsInStoic() {
            return getReactantsWithMolfile(getStoicReactants()).length === 0;
        }

        function analyzeRxn() {
            getMissingReactionReactantsInStoic(vm.infoReactants).then(function(batchesToSearch) {
                if (batchesToSearch && batchesToSearch.length) {
                    $uibModal.open({
                        animation: true,
                        size: 'lg',
                        controller: 'AnalyzeRxnController',
                        controllerAs: 'vm',
                        templateUrl: 'scripts/indigo-components/common/analyze-rxn/analyze-rxn.html',
                        resolve: {
                            reactants: function() {
                                var reactants = _.map(batchesToSearch, function(batch) {
                                    var batchCopy = angular.copy(batch);
                                    CalculationService.getImageForStructure(batchCopy.structure.molfile, 'molecule', function(image) {
                                        batchCopy.structure.image = image;
                                    });

                                    return batchCopy;
                                });

                                return reactants.length ? reactants : vm.infoReactants;
                            },
                            onStoichRowsChanged: function() {
                                return updateReactants;
                            }
                        }
                    });
                } else {
                    AlertModal.info('Stoichiometry is synchronized', 'sm');
                }
            });
        }

        function createRxn() {
            var REACTANT = AppValues.getRxnRoleReactant().name;
            var stoicReactantsMolfiles = _.compact(_.map(getStoicReactants(), function(batch) {
                return batch.rxnRole.name === REACTANT && batch.structure.molfile;
            }));
            var intendedProductsMolfiles = _.compact(_.map(getIntendedProducts(), function(batch) {
                return batch.structure.molfile;
            }));
            CalculationService.combineReactionComponents(stoicReactantsMolfiles, intendedProductsMolfiles)
                .then(function(result) {
                    $rootScope.$broadcast('new-reaction-scheme', result.data);
                });
        }

        function searchReagents(activeTab) {
            $uibModal.open({
                animation: true,
                size: 'lg',
                controller: 'SearchReagentsController',
                controllerAs: 'vm',
                templateUrl: 'scripts/indigo-components/common/search-reagents/search-reagents.html',
                resolve: {
                    activeTab: function() {
                        return activeTab;
                    }
                }
            });
        }

        function init() {
            vm.reactantsColumns = [
                getCompoundId(),
                stoichReactantsColumns.casNumber,
                stoichReactantsColumns.chemicalName,
                stoichReactantsColumns.fullNbkBatch,
                stoichReactantsColumns.molWeight,
                stoichReactantsColumns.weight,
                stoichReactantsColumns.volume,
                stoichReactantsColumns.mol,
                stoichReactantsColumns.eq,
                getLimiting(),
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

            vm.productsColumns = [
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

            bindEvents();
        }

        function onCloseCell(column, data) {
            if (_.includes(columnsWithClose, column.id)) {
                CalculationService.setEntered(data);
                if (column.id === stoichReactantsColumns.rxnRole.id) {
                    onRxnRoleChange(data);
                }
                CalculationService.recalculateStoichBasedOnBatch(data).then(updateReactantsAndProducts);
            }
        }

        function getLimiting() {
            return _.extend({}, stoichReactantsColumns.limiting, {
                onClick: function(data) {
                    CalculationService.setEntered(data);
                    CalculationService.recalculateStoichBasedOnBatch(data).then(updateReactantsAndProducts);
                }
            });
        }

        function getCompoundId() {
            return _.extend({}, stoichReactantsColumns.compoundId, {
                onClose: function(data) {
                    var row = data.row;
                    var compoundId = data.model;
                    stoichColumnActions.fetchBatchByCompoundId(compoundId, row)
                        .then(function() {
                            vm.onPrecursorsChanged({precursors: getPrecursors()});
                        }, alertCompoundWrongFormat);
                }
            });
        }

        function alertCompoundWrongFormat() {
            notifyService.error('Compound does not exist or in the wrong format');
        }

        function getStoicTable() {
            return vm.model.stoichTable;
        }

        function getStoicReactants() {
            return vm.model.stoichTable.reactants;
        }

        function getIntendedProducts() {
            return vm.model.stoichTable.products;
        }

        function setStoicReactants(reactants) {
            vm.model.stoichTable.reactants = reactants;
            vm.onChangedReactants({reactants: reactants});
            vm.onChanged();
        }

        function setIntendedProducts(products) {
            vm.model.stoichTable.products = products;
        }

        function addStoicReactant(reactant) {
            vm.model.stoichTable.reactants.push(reactant);
            vm.onChangedReactants({reactants: vm.model.stoichTable.reactants});
            vm.onChanged();
        }

        function onRxnRoleChange(data) {
            var SOLVENT = AppValues.getRxnRoleSolvent().name;
            if (data.model.name === SOLVENT) {
                var valuesToDefault = ['weight', 'mol', 'eq', 'density', 'stoicPurity'];
                CalculationService.resetValuesToDefault(valuesToDefault, data.row);
                CalculationService.setValuesReadonly(['weight', 'mol', 'eq', 'density'], data.row);
            } else if (data.model.name !== SOLVENT && data.oldVal === SOLVENT) {
                CalculationService.resetValuesToDefault(['volume', 'molarity'], data.row);
                CalculationService.setValuesEditable(['weight', 'mol', 'eq', 'density'], data.row);
            }
        }

        function getStructureImagesForIntendedProducts() {
            _.each(getIntendedProducts(), function(item) {
                if (!item.structure.image) {
                    CalculationService.getImageForStructure(item.structure.molfile, 'molecule', function(image) {
                        item.structure.image = image;
                    });
                }
            });
        }

        function isEqualsMolfiles(reaction) {
            return _.some(reaction, function(reagent, index) {
                return _.get(reagent.structure, 'molfile') === _.get(vm.model.stoichTable, 'products[' + index + '].structure.molfile');
            });
        }

        function getReactionProductsAndReactants(reaction) {
            if (!reaction || isEqualsMolfiles(reaction)) {
                return;
            }

            if (vm.infoProducts && vm.infoProducts.length) {
                setIntendedProducts(vm.infoProducts);
                getStructureImagesForIntendedProducts();
            }

            CalculationService.recalculateStoich();
        }

        function bindEvents() {
            $scope.$watch('vm.infoProducts', getReactionProductsAndReactants, true);

            $scope.$watch('vm.model.stoichTable', function(stoichTable) {
                StoichTableCache.setStoicTable(stoichTable);
            });

            $scope.$watch('vm.model.stoichTable.products', function(products) {
                _.each(products, function(batch) {
                    if (!batch.$$batchHash) {
                        batch.$$batchHash = batch.formula + batch.exactMass;
                    }
                });
                vm.onChangedProducts({products: products});
            }, true);

            $scope.$on('stoich-rows-changed', function(event, data) {
                updateReactants(data);
            });
        }

        function updateReactantsAndProducts(data) {
            var newReactants = data.stoicBatches;
            var newProducts = data.intendedProducts;
            if (getStoicReactants() && newReactants.length === getStoicReactants().length) {
                _.each(getStoicReactants(), function(reactant, i) {
                    _.extend(reactant, newReactants[i]);
                });
            }
            if (getIntendedProducts() && newProducts.length === getIntendedProducts().length) {
                _.each(getIntendedProducts(), function(product, i) {
                    _.extend(product, newProducts[i]);
                });
            }
        }

        function updateReactants(reactants) {
            if (reactants && getStoicReactants() && reactants.length === getStoicReactants().length) {
                return;
            }
            setStoicReactants(_.unionWith(getStoicReactants(), reactants, angular.equals));
            vm.onPrecursorsChanged({precursors: getPrecursors()});
            CalculationService.recalculateStoich();
        }

        function getPrecursors() {
            return _.compact(_.map(getReactantsWithMolfile(getStoicReactants()), function(r) {
                return r.compoundId || r.fullNbkBatch;
            })).join(', ');
        }

        function getReactantsWithMolfile(stoichReactants) {
            return _.filter(stoichReactants, function(item) {
                return item.structure && item.structure.molfile && isReactant(item);
            });
        }

        function isReactant(item) {
            return item.rxnRole.name === 'REACTANT';
        }

        function isReactantAlreadyInStoic(responces) {
            return _.some(responces);
        }

        function getMissingReactionReactantsInStoic(reactantsInfo) {
            var reactantsToSearch = [];
            var stoicReactants = getReactantsWithMolfile(getStoicReactants());

            if (_.isEmpty(reactantsInfo) || stoicReactants.length !== reactantsInfo.length) {
                return $q.resolve(reactantsInfo);
            }

            var allPromises = _.map(reactantsInfo, function(reactantInfo) {
                var reactantsEqualityPromises = _.map(stoicReactants, function(stoicReactant) {
                    return CalculationService.isMoleculesEqual(stoicReactant.structure.molfile, reactantInfo.structure.molfile);
                });

                return $q.all(reactantsEqualityPromises).then(function(responces) {
                    if (!isReactantAlreadyInStoic(responces)) {
                        reactantsToSearch.push(reactantInfo);
                    }
                });
            });

            return $q.all(allPromises).then(function() {
                return reactantsToSearch;
            });
        }
    }
})();
