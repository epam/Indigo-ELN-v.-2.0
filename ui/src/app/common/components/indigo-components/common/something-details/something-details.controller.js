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

SomethingDetailsController.$inject = ['$scope', 'usersService'];

function SomethingDetailsController($scope, usersService) {
    var vm = this;
    var userPromise;

    init();

    function init() {
        userPromise = usersService.get().then(function(dictionary) {
            vm.users = dictionary.words;
        });

        vm.updateIds = updateIds;

        bindEvents();
    }

    function bindEvents() {
        $scope.$watch('vm.componentData.experimentCreator', function(experimentCreator) {
            if (experimentCreator) {
                userPromise.then(function() {
                    vm.experimentCreator = _.find(vm.users, {id: vm.componentData.experimentCreator});
                });
            }
        });

        $scope.$watch('vm.componentData.coAuthors', function(coAuthors) {
            if (coAuthors) {
                userPromise.then(function() {
                    vm.coAuthors = usersService.getUsersById(vm.componentData.coAuthors);
                });
            }
        });

        $scope.$watch('vm.componentData.designers', function(designers) {
            if (designers) {
                userPromise.then(function() {
                    vm.designers = usersService.getUsersById(vm.componentData.designers);
                });
            }
        });

        $scope.$watch('vm.componentData.batchOwner', function(batchOwner) {
            if (batchOwner) {
                userPromise.then(function() {
                    vm.batchOwner = usersService.getUsersById(vm.componentData.batchOwner);
                });
            }
        });
    }

    function updateIds(property, selectedValues) {
        vm.componentData[property] = _.map(selectedValues, 'id');
    }
}

module.exports = SomethingDetailsController;
