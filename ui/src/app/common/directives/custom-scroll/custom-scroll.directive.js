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

customScroll.$inject = ['simpleLocalCache', '$timeout'];

function customScroll(simpleLocalCache, $timeout) {
    return {
        restrict: 'A',
        scope: {
            customScroll: '@'
        },
        link: link,
        controller: angular.noop,
        controllerAs: 'vm',
        bindToController: true
    };

    function link($scope, $element, $attributes, vm) {
        var scrollLocation = simpleLocalCache.getByKey(vm.customScroll) || 0;
        var el = $element[0];
        var scrollPosition = 0;

        $timeout(function() {
            $element.scrollTop(scrollLocation);
            angular.getTestability($element).whenStable(function() {
                $element.scrollTop(scrollLocation);
            });
        });

        $element.on('scroll', function() {
            scrollPosition = el.scrollTop;
        });

        $scope.$on('$destroy', function() {
            simpleLocalCache.putByKey(vm.customScroll, scrollPosition);
        });
    }
}

module.exports = customScroll;
