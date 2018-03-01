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

require('./columns-settings.less');

var template = require('./columns-settings.html');

function columnsSettings() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            columns: '=',
            visibleColumns: '=',
            onChanged: '&',
            resetColumns: '&'
        },
        controller: angular.noop,
        controllerAs: 'vm',
        bindToController: true
    };
}

module.exports = columnsSettings;
