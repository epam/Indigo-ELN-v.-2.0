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

var PerfectScrollbar = require('perfect-scrollbar/dist/perfect-scrollbar');

indigoTabScroller.$inject = ['$timeout'];

function indigoTabScroller($timeout) {
    return {
        restrict: 'A',
        link: link
    };

    /* @ngInject */
    function link($scope, $element) {
        $element.addClass('indigo-scroller');

        var perfectScrollbar = new PerfectScrollbar($element[0], {
            suppressScrollY: true,
            useBothWheelAxes: true
        });

        // Update scrollbar to display immediately
        $timeout(function() {
            perfectScrollbar.update();
        }, 0);

        // Update scrollbar on container resize
        $scope.$watch(function() {
            return $scope.vm.tabs && _.keys($scope.vm.tabs).length;
        }, function() {
            perfectScrollbar.update();
        });

        $scope.$watch('vm.activeTab', function() {
            $timeout(function() {
                var targetElement = $element.find('.active');

                targetElement[0].scrollIntoView();
            }, 100);
        });

        $scope.$on('$destroy', function() {
            perfectScrollbar.destroy();
            perfectScrollbar = null;
        });
    }
}

module.exports = indigoTabScroller;

