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

nestedOutsideClick.$inject = ['$parse', '$document', '$timeout'];

function nestedOutsideClick($parse, $document, $timeout) {
    return {
        link: function($scope, $element, attr) {
            var expression = $parse(attr.nestedOutsideClick);
            $timeout(function() {
                $document.on('click', handler);
            });

            $scope.$on('$destroy', function() {
                $document.off('click', handler);
            });

            function handler(event) {
                if ($element[0] !== event.target && !includes($element[0].children, event.target)) {
                    $scope.$apply(function() {
                        expression($scope, {$event: event});
                    });
                }
            }
        }
    };

    function includes(children, target) {
        return _.some(children, function(child) {
            return child === target || (child.children && child.children.length && includes(child.children, target));
        });
    }
}

module.exports = nestedOutsideClick;
