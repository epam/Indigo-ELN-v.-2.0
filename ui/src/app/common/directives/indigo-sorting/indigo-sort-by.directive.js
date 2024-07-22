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

function indigoSortBy() {
    return {
        restrict: 'A',
        scope: false,
        require: '^indigoSort',
        link: link
    };

    function link($scope, $element, $attr, parentCtrl) {
        var iconElement = $element.find('.glyphicon');

        $element.bind('click', function() {
            parentCtrl.sort($attr.indigoSortBy);
        });

        $scope.$watch(function() {
            return parentCtrl.isAscending + parentCtrl.indigoSort;
        }, updateClasses);

        function updateClasses() {
            var isCurrent = parentCtrl.indigoSort === $attr.indigoSortBy;
            iconElement.toggleClass('glyphicon-sort-by-attributes', isCurrent && parentCtrl.isAscending);
            iconElement.toggleClass('glyphicon-sort-by-attributes-alt', isCurrent && !parentCtrl.isAscending);
        }
    }
}

module.exports = indigoSortBy;

