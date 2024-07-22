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

var template = require('./inline-loader.html');

function indigoInlineLoader() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            promise: '=',
            onStatusChanged: '&'
        },
        controller: IndigoInlineLoaderController,
        controllerAs: 'vm',
        bindToController: true
    };
}

IndigoInlineLoaderController.$inject = ['$scope'];

function IndigoInlineLoaderController($scope) {
    var vm = this;

    init();

    function init() {
        bindEvents();
    }

    function bindEvents() {
        $scope.$watch('vm.promise.$$state', function(val) {
            vm.isLoading = (val && val.status === 0);
            if (vm.onStatusChanged) {
                vm.onStatusChanged({completed: !vm.isLoading});
            }
        }, true);
    }
}

module.exports = indigoInlineLoader;
