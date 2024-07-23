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

var template = require('./confirmation-modal.html');

/* @ngInject */
function confirmationModal($uibModal, $q) {
    return {
        open: open,
        openEntityVersionsConflictConfirm: openEntityVersionsConflictConfirm
    };

    function open(resolve, size) {
        return $uibModal.open({
            bindToController: true,
            controllerAs: 'vm',
            resolve: resolve,
            size: size || 'md',
            template: template,
            controller: 'ConfirmationModalController'
        });
    }

    function generateWarningMessage(entityName) {
        return entityName +
            ' has been changed by another user while you have not applied changes. ' +
            'You can Accept or Reject saved changes. ' +
            '"Accept" button reloads page to show saved data,' +
            ' "Reject" button leave entered data and allows you to save them.';
    }

    function openEntityVersionsConflictConfirm(entityName) {
        return open({
            title: function() {
                return 'Warning';
            },
            message: function() {
                return generateWarningMessage(entityName);
            },
            buttons: function() {
                return {
                    yes: 'Accept',
                    no: 'Reject'
                };
            }
        }).result.then(function(status) {
            return status === 'resolve' ? $q.resolve() : $q.reject();
        });
    }
}

module.exports = confirmationModal;
