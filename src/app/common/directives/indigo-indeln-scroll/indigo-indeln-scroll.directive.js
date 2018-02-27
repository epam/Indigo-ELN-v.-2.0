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

indigoIndelnScroll.$inject = ['$document', '$window'];

function indigoIndelnScroll($document, $window) {
    return {
        restrict: 'A',
        link: link
    };

    function link(scope, iAttrs) {
        var scrollToTop = function() {
            var h = angular.element($window).height();
            var elDocument = angular.element($document);

            elDocument.mousemove(function(e) {
                var mousePosition = e.pageY - angular.element($window).scrollTop();
                var topRegion = 220;
                var bottomRegion = h - 220;
                if (isClickDown(e, mousePosition, topRegion, bottomRegion)) {
                    var distance = e.clientY - (h / 2);
                    // <- velocity
                    distance *= 0.1;
                    angular.element('#entities-content-id').scrollTop(distance + elDocument.scrollTop());
                } else {
                    angular.element('#entities-content-id').unbind('mousemove');
                }
            });

            function isClickDown(e, mousePosition, topRegion, bottomRegion) {
                return e.which === 1 && (mousePosition < topRegion || mousePosition > bottomRegion);
            }
        };
        if (iAttrs.dragulaModel) {
            scope.$on(iAttrs.dragulaModel + '.drag', scrollToTop);
        }
    }
}

module.exports = indigoIndelnScroll;

