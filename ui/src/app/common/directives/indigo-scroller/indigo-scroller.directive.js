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

require('./indigo-scroller.less');
var PerfectScrollbar = require('perfect-scrollbar/dist/perfect-scrollbar');

/* @ngInject */
function indigoScroller($timeout) {
    return {
        restrict: 'A',
        link: link
    };

    function link($scope, $element, $attr) {
        $element.addClass('indigo-scroller indigo-scroller-axis-' + $attr.indigoScroller);

        var perfectScrollbar = new PerfectScrollbar($element[0], {
            useBothWheelAxes: true
        });

        // Update scrollbar to display immediately
        $timeout(function() {
            perfectScrollbar.update();
        }, 0);

        $scope.$on('$destroy', function() {
            perfectScrollbar.destroy();
            perfectScrollbar = null;
        });
    }
}

module.exports = indigoScroller;
