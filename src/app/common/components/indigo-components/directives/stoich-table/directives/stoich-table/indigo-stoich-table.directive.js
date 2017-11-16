var template = require('./indigo-stoich-table.html');
var analyzeRxnTemplate = require('../../../../common/analyze-rxn/analyze-rxn.html');
var searchReagentsTemplate = require('../../../../common/search-reagents/search-reagents.html');

function indigoStoichTable() {
    return {
        restrict: 'E',
        replace: true,
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

IndigoStoichTableController.$inject = [
    '$scope', '$rootScope', '$q', '$uibModal', 'appValuesService', 'stoichColumnActions', 'alertModal',
    'notifyService', 'calculationService', 'stoichTableCache', 'stoichReactantsColumns',
    'stoichProductColumns'
];

function IndigoStoichTableController($scope, $rootScope, $q, $uibModal, appValuesService, stoichColumnActions,
                                     alertModal, notifyService, calculationService, stoichTableCache,
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
        'rxnRole',
        'compoundId'
    ];

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
        vm.selectedRow.rxnRole = appValuesService.getRxnRoleReactant();
        vm.onChanged();
    }

    function appendRow() {
        var reactant = calculationService.createBatch(vm.componentData, false);
        addStoicReactant(reactant);
    }

    function removeRow() {
        setStoicReactants(_.without(vm.componentData.reactants, vm.selectedRow));
        calculationService.recalculateStoich();
        vm.onChanged();
    }

    function onRowSelected(row) {
        vm.selectedRow = row || null;
    }

    function noReactantsInStoic() {
        return getReactantsWithMolfile(vm.componentData.reactants).length === 0;
    }

    function analyzeRxn() {
        getMissingReactionReactantsInStoic(vm.infoReactants).then(function(reactantsToSearch) {
            if (!_.isEmpty(reactantsToSearch)) {
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
                        onStoichRowsChanged: function() {
                            return function(reactants) {
                                _.forEach(reactants, function(reactant) {
                                    vm.componentData.reactants.push(angular.copy(reactant));
                                });
                                checkLimiting();
                            };
                        }
                    }
                });
            } else {
                alertModal.info('Stoichiometry is synchronized', 'sm');
            }
        });
    }

    function checkLimiting() {
        var limiting = calculationService.findLimiting(vm.componentData.reactants);
        if (!limiting && vm.componentData.reactants.length) {
            _.first(vm.componentData.reactants).limiting = true;
        }
    }

    function createRxn() {
        var REACTANT = appValuesService.getRxnRoleReactant().name;
        var stoicReactantsMolfiles = _.compact(_.map(vm.componentData.reactants, function(batch) {
            return batch.rxnRole.name === REACTANT && batch.structure.molfile;
        }));
        var intendedProductsMolfiles = _.compact(_.map(getIntendedProducts(), function(batch) {
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
                }
            }
        });
    }

    function init() {
        vm.reactantsColumns = [
            stoichReactantsColumns.compoundId,
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
            if (column.id === stoichReactantsColumns.compoundId.id) {
                onCloseCompoundId(data);
            }
            calculationService.setEntered(data);
            if (column.id === stoichReactantsColumns.rxnRole.id) {
                onRxnRoleChange(data);
            }
            calculationService.recalculateStoichBasedOnBatch(data).then(updateReactantsAndProducts);
        }
    }

    function getLimiting() {
        return _.extend({}, stoichReactantsColumns.limiting, {
            onClick: function(data) {
                calculationService.setEntered(data);
                calculationService.recalculateStoichBasedOnBatch(data).then(updateReactantsAndProducts);
            }
        });
    }

    function onCloseCompoundId(data) {
        var row = data.row;
        var compoundId = data.model;
        stoichColumnActions.fetchBatchByCompoundId(compoundId, row)
            .then(function() {
                vm.onPrecursorsChanged({precursors: getPrecursors()});
            }, alertCompoundWrongFormat);
    }

    function alertCompoundWrongFormat() {
        notifyService.error('Compound does not exist or in the wrong format');
    }

    function getIntendedProducts() {
        return vm.componentData.products;
    }

    function setStoicReactants(reactants) {
        vm.componentData.reactants = reactants;
        checkLimiting();
        vm.onChangedReactants({reactants: reactants});
        vm.onPrecursorsChanged({precursors: getPrecursors()});
        vm.onChanged();
    }

    function setIntendedProducts(products) {
        vm.componentData.products = products;
    }

    function addStoicReactant(reactant) {
        vm.componentData.reactants.push(reactant);
        vm.onChangedReactants({reactants: vm.componentData.reactants});
        vm.onChanged();
    }

    function onRxnRoleChange(data) {
        var SOLVENT = appValuesService.getRxnRoleSolvent().name;
        if (data.model.name === SOLVENT) {
            var valuesToDefault = ['weight', 'mol', 'eq', 'density', 'stoicPurity'];
            calculationService.resetValuesToDefault(valuesToDefault, data.row);
            calculationService.setValuesReadonly(['weight', 'mol', 'eq', 'density'], data.row);
        } else if (data.model.name !== SOLVENT && data.oldVal === SOLVENT) {
            calculationService.resetValuesToDefault(['volume', 'molarity'], data.row);
            calculationService.setValuesEditable(['weight', 'mol', 'eq', 'density'], data.row);
        }
    }

    function getStructureImagesForIntendedProducts() {
        _.each(getIntendedProducts(), function(item) {
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
            setIntendedProducts(vm.infoProducts);
            getStructureImagesForIntendedProducts();
        }

        calculationService.recalculateStoich();
    }

    function bindEvents() {
        $scope.$watch('vm.infoProducts', getReactionProductsAndReactants, true);

        $scope.$watch('vm.componentData', function(stoichTable) {
            stoichTableCache.setStoicTable(stoichTable);
        });

        $scope.$watch('vm.componentData.products', function(products) {
            _.each(products, function(batch) {
                if (!batch.$$batchHash) {
                    batch.$$batchHash = batch.formula + batch.exactMass;
                }
            });
            vm.onChangedProducts({products: products});
        }, true);

        $scope.$on('stoich-rows-changed', function(event, data) {
            addStoicReactant(data);
            calculationService.recalculateStoich();
        });
    }

    function updateReactantsAndProducts(data) {
        var newReactants = data.stoicBatches;
        var newProducts = data.intendedProducts;
        if (vm.componentData.reactants && newReactants.length === vm.componentData.reactants.length) {
            _.each(vm.componentData.reactants, function(reactant, i) {
                _.extend(reactant, newReactants[i]);
            });
        }
        if (getIntendedProducts() && newProducts.length === getIntendedProducts().length) {
            _.each(getIntendedProducts(), function(product, i) {
                _.extend(product, newProducts[i]);
            });
        }
    }

    function getPrecursors() {
        return _.compact(_.map(getReactantsWithMolfile(vm.componentData.reactants), function(r) {
            return r.compoundId || r.fullNbkBatch;
        })).join(', ');
    }

    function isReactantWithMolfile(item) {
        return item.structure && item.structure.molfile && isReactant(item);
    }

    function getReactantsWithMolfile(stoichReactants) {
        return _.filter(stoichReactants, function(item) {
            return isReactantWithMolfile(item);
        });
    }

    function isReactant(item) {
        return item.rxnRole.name === 'REACTANT';
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
        var stoicReactants = getReactantsWithMolfile(vm.componentData.reactants);

        if (_.isEmpty(reactantsInfo) || stoicReactants.length !== reactantsInfo.length) {
            _.forEach(reactantsInfo, function(reactant) {
                if (!findLikedReactant(reactant) && isReactantWithMolfile(reactant)) {
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
