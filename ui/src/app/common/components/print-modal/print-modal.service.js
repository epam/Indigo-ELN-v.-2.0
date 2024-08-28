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

var template = require('./print-modal.html');

/* @ngInject */
function printModal($uibModal, $window, $httpParamSerializer, apiUrl) {
    var dlg;

    return {
        showPopup: showPopup,
        close: close
    };

    function showPopup(params, resourceType) {
        dlg = $uibModal.open({
            animation: true,
            template: template,
            controller: 'PrintModalController',
            controllerAs: 'vm',
            resolve: {
                params: params,
                resourceType: function() {
                    return resourceType;
                }
            }
        });

        return dlg.result.then(function(result) {
            var qs = $httpParamSerializer(result);
            var url = apiUrl + 'print/project/' + params.projectId;
            if (params.notebookId) {
                url += '/notebook/' + params.notebookId;
            }
            if (params.experimentId) {
                url += '/experiment/' + params.experimentId;
            }
            url += '?' + qs;
            $window.open(url);
        });
    }

    function close() {
        if (dlg) {
            dlg.dismiss();
            dlg = null;
        }
    }
}

module.exports = printModal;
