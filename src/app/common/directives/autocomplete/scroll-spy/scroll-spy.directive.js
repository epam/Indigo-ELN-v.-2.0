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

function scrollSpy() {
    return {
        restrict: 'A',
        link: function($scope, $element, $attrs) {
            var raw = $element[0];

            $element.bind('scroll', function() {
                // Calls function when element is scrolled to the bottom
                var isScrolledToBottom = raw.scrollTop + raw.offsetHeight >= raw.scrollHeight;

                if (isScrolledToBottom && $attrs.scrollSpy) {
                    $scope.$apply($attrs.scrollSpy);
                }
            });

            $scope.$on('$destroy', function() {
                $element.unbind('scroll');
            });
        }
    };
}

module.exports = scrollSpy;
