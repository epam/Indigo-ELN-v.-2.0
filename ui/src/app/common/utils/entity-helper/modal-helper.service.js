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

var newExperimentModal = require('../../../experiment/create-new-experiment-modal/create-new-experiment-modal.html');
var newNotebookModal = require('../../../notebook/select-parent/notebook-select-parent.html');

/* @ngInject */
function modalHelper($uibModal) {
    var dlg;

    return {
        openCreateNewExperimentModal: openCreateNewExperimentModal,
        openCreateNewNotebookModal: openCreateNewNotebookModal,
        close: close
    };

    function openCreateNewExperimentModal(resolve) {
        close();
        dlg = $uibModal.open({
            animation: true,
            template: newExperimentModal,
            controller: 'CreateNewExperimentModalController',
            controllerAs: 'vm',
            size: 'lg',
            resolve: resolve
        });

        return dlg.result;
    }

    function openCreateNewNotebookModal(resolve) {
        close();
        dlg = $uibModal.open({
            animation: true,
            template: newNotebookModal,
            controller: 'NotebookSelectParentController',
            controllerAs: 'vm',
            size: 'lg',
            resolve: resolve
        });

        return dlg.result;
    }

    function close() {
        if (dlg) {
            dlg.dismiss();
            dlg = null;
        }
    }
}

module.exports = modalHelper;
