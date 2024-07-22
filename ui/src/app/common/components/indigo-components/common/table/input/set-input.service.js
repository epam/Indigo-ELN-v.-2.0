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

var template = require('./set-input-value.html');

setInputService.$inject = ['$uibModal', 'registrationUtil'];

function setInputService($uibModal, registrationUtil) {
    return {
        getActions: function(name) {
            return [{
                name: 'Set value for ' + name,
                title: name,
                action: function(rows, column) {
                    action(rows, name, column.id);
                }
            }];
        }
    };

    function action(rows, title, columnId) {
        $uibModal.open({
            template: template,
            controller: 'SetInputValueController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                name: function() {
                    return title;
                }
            }
        }).result.then(function(result) {
            _.each(rows, function(row) {
                if (!registrationUtil.isRegistered(row)) {
                    row[columnId] = result;
                }
            });
        });
    }
}

module.exports = setInputService;
