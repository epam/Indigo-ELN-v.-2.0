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

/* @ngInject */
function AnalyzeRxnController($uibModalInstance, reactants, searchService, appValuesService, addTableRowsCallback,
                              stoichColumnActions, $q) {
    var vm = this;
    vm.addToStoichTable = addToStoichTable;
    vm.updateStoicAndExit = updateStoicAndExit;
    vm.search = search;
    vm.cancel = cancel;

    $onInit();

    function $onInit() {
        vm.model = getDefaultModel();
        vm.tabs = buildTabs();
        vm.selectedReactants = [];
        vm.countSelectedReactants = 0;

        vm.onSelected = onSelected;
    }

    function onSelected(tab, item) {
        tab.selectedReactant = item;
        vm.countSelectedReactants += (item ? 1 : -1);
        vm.selectedReactants = buildReactantsFromSelected();
    }

    function buildTabs() {
        return _.map(reactants, function(reactant, id) {
            return {
                formula: reactant.formula,
                molfile: reactant.structure.molfile,
                searchResult: [],
                selectedReactant: null,
                id: id
            };
        });
    }

    function addToStoichTable() {
        addTableRowsCallback(vm.selectedReactants);
    }

    function buildReactantsFromSelected() {
        return _.map(vm.tabs, function(tab) {
            var copied = angular.copy(reactants[tab.id]);

            return stoichColumnActions.cleanReactant(tab.selectedReactant ?
                _.extend(copied, tab.selectedReactant)
                : copied);
        });
    }

    function updateStoicAndExit() {
        addTableRowsCallback(buildReactantsFromSelected());
        $uibModalInstance.close({});
    }

    function search() {
        vm.loading = true;
        $q.all(_.map(vm.tabs, function(tab) {
            return getSearchResult(tab.molfile)
                .then(function(searchResult) {
                    tab.searchResult = responseCallback(searchResult);
                });
        })).finally(function() {
            vm.loading = false;
            vm.isSearchCompleted = true;
        });
    }

    function cancel() {
        $uibModalInstance.close({});
    }

    function getDefaultModel() {
        return {
            reactants: reactants,
            selectedReactants: [],
            isSearchResultFound: false,
            databases: searchService.getCatalogues()
        };
    }

    function prepareDatabases() {
        return _.map(_.filter(vm.model.databases, {
            isChecked: true
        }), 'value');
    }

    function responseCallback(result) {
        var databases = prepareDatabases();

        return _.map(result, function(item) {
            var batchDetails = _.extend({}, item.details);
            batchDetails.$$isCollapsed = true;
            batchDetails.$$isSelected = false;
            batchDetails.nbkBatch = item.notebookBatchNumber;
            batchDetails.database = databases.join(', ');
            batchDetails.rxnRole = batchDetails.rxnRole || appValuesService.getRxnRoleReactant();
            batchDetails.saltCode = batchDetails.saltCode || appValuesService.getDefaultSaltCode();

            return batchDetails;
        });
    }

    function getSearchResult(molfile) {
        var databases = prepareDatabases();
        var searchRequest = {
            databases: databases,
            structure: {
                molfile: molfile, searchMode: 'substructure'
            }
        };

        return searchService.search(searchRequest).$promise;
    }
}

module.exports = AnalyzeRxnController;
