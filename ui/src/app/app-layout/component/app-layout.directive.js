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

var template = require('./app-layout.html');

function appLayout() {
    return {
        scope: true,
        restrict: 'E',
        controller: AppPageController,
        controllerAs: 'vm',
        template: template,
        bindToController: true
    };

    /* @ngInject */
    function AppPageController($rootScope, $scope, $cookies, $window, wsService, $timeout) {
        var vm = this;
        var mobileViewWidth = 992;
        var updateToggle;
        var windowElement;
        var subscribers = [];

        init();

        function init() {
            vm.isOpen = true;
            windowElement = angular.element($window);
            updateToggle = createDebounce();

            vm.toggleSidebar = toggleSidebar;
            bindEvents();
        }

        function toggleSidebar() {
            vm.isOpen = !vm.isOpen;
            $cookies.put('toggle', vm.isOpen);
        }

        function bindSubscribes() {
            onSubscribe('/topic/registration_status', 'batch-registration-status-changed');
            onSubscribe('/user/topic/entity_updated', 'entity-updated');
            onSubscribe('/user/topic/sub_entity_changes', 'sub_entity_changes');
        }

        function onSubscribe(destination, broadcastEventName) {
            wsService.subscribe(destination).then(function(subscribe) {
                subscribers.push(subscribe);
                subscribe.onServerEvent(function(statuses) {
                    $rootScope.$broadcast(broadcastEventName, statuses);
                });
            });
        }

        function bindEvents() {
            windowElement.bind('resize', updateToggle);
            bindSubscribes();

            $scope.$on('$destroy', function() {
                _.forEach(subscribers, function(subscribe) {
                    subscribe.unSubscribe();
                });
                windowElement.off('resize', updateToggle);
            });
        }

        function createDebounce() {
            return _.debounce(function() {
                $timeout(function() {
                    if (windowElement.width() >= mobileViewWidth) {
                        if (!_.isUndefined($cookies.get('toggle'))) {
                            vm.isOpen = $cookies.get('toggle');
                        } else {
                            vm.isOpen = true;
                        }
                    } else {
                        vm.isOpen = false;
                    }
                });
            }, 100);
        }
    }
}

module.exports = appLayout;
