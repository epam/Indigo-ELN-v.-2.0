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

/* @ngInject */
function indigoTabScroller($timeout, $window) {
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

        // Update scrollbar in next angular digest
        function update() {
            $timeout(function() {
                perfectScrollbar.update();
            }, 0);
        }

        $scope.$watchCollection('vm.tabs', update);

        $scope.$watch('vm.activeTab.$$title', update);

        $scope.$watch('vm.activeTab', function() {
            $timeout(function() {
                var targetElement = $element.find('.active');

                targetElement[0].scrollIntoView();
            }, 100);
        });

        angular.element($window).bind('resize', update);

        $scope.$on('$destroy', function() {
            angular.element($window).unbind('resize', update);
            perfectScrollbar.destroy();
            perfectScrollbar = null;
        });

        update();
    }
}

module.exports = indigoTabScroller;

