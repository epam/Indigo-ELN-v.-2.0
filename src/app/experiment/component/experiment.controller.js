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
function ExperimentController($scope, dashboardService, configService, $filter) {
    var vm = this;
    var openExperiments;
    var waitingExperiments;
    var submittedExperiments;

    vm.experiments = [];
    vm.dView = 'open';
    vm.itemsPerPage = 20;
    vm.signatureServiceUrl = configService.getConfiguration()['indigoeln.client.signatureservice.url'];
    vm.sortBy = {
        field: 'lastEditDate',
        isAscending: false
    };

    vm.loadAll = loadAll;
    vm.onViewChange = onViewChange;
    vm.sort = sort;
    vm.doPage = doPage;
    vm.refresh = refresh;
    vm.getIdleWorkdays = getIdleWorkdays;
    vm.clear = clear;

    vm.loadAll();

    function loadAll() {
        vm.loading = dashboardService.get({}, function(result) {
            openExperiments = result.openAndCompletedExp.filter(function(e) {
                return e.status === 'Open';
            });
            waitingExperiments = result.waitingSignatureExp;
            submittedExperiments = result.submittedAndSigningExp;

            _.forEach(waitingExperiments, function(exp) {
                exp.idleWorkdays = getIdleWorkdays(exp.creationDate);
            });
            _.forEach(openExperiments, function(exp) {
                exp.idleWorkdays = getIdleWorkdays(exp.creationDate);
            });
            _.forEach(submittedExperiments, function(exp) {
                exp.idleWorkdays = getIdleWorkdays(exp.creationDate);
            });
            vm.openExperimentsLength = openExperiments.length;
            vm.waitingExperimentsLength = waitingExperiments.length;
            vm.submittedExperimentsLength = submittedExperiments.length;
            vm.onViewChange();
        }).$promise;
    }

    function onViewChange() {
        if (vm.dView === 'open') {
            vm.curExperiments = openExperiments;
        } else if (vm.dView === 'wait') {
            vm.curExperiments = waitingExperiments;
        } else if (vm.dView === 'submitted') {
            vm.curExperiments = submittedExperiments;
        }
        vm.totalItems = vm.curExperiments.length;
        vm.sort(vm.sortBy.field, false, true);
    }

    function sort(predicate, isAscending, noDigest) {
        vm.sortBy.field = predicate;
        vm.sortBy.isAscending = isAscending;
        vm.curExperiments = $filter('orderBy')(vm.curExperiments, predicate, !isAscending);
        vm.doPage(1);
        if (!noDigest) {
            $scope.$apply();
        }
    }

    function doPage(page) {
        if (page) {
            vm.page = page;
        } else {
            page = vm.page;
        }

        var ind = (page - 1) * vm.itemsPerPage;
        vm.curExperimentsPaged = vm.curExperiments.slice(ind, ind + vm.itemsPerPage);
    }

    function refresh() {
        vm.loadAll();
        vm.clear();
    }

    function getIdleWorkdays(creationDate) {
        return Math.round((new Date() - Date.parse(creationDate)) / (1000 * 60 * 60 * 24));
    }

    function clear() {
        vm.experiment = {
            name: null,
            experimentNumber: null,
            templateId: null,
            id: null
        };
    }
}

module.exports = ExperimentController;
