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

var template = require('../../components/file-uploader/single-file-uploader/single-file-uploader-modal.html');

/* @ngInject */
function sdImportService($uibModal, dictionaryService, sdConstants,
                         alertModal, apiUrl, sdImportHelper, $q) {
    var dlg;

    return {
        importFile: importFile,
        closeDialog: close
    };

    function importValues(sdUnitToImport, property, index, dicts, itemToImport) {
        var propCode = getPropertyCode(property, index);
        var value = sdUnitToImport.properties[propCode];
        if (value) {
            var formattedProperty = sdImportHelper.formatProperty(property, value, dicts, index);
            if (itemToImport[property.name]) {
                _.defaultsDeep(itemToImport[property.name], formattedProperty);
            } else {
                itemToImport[property.name] = formattedProperty;
            }
        }
    }

    function getFilledProperties(sdUnitToImport, dicts) {
        if (sdUnitToImport.properties) {
            var itemToImport = {};
            _.forEach(sdConstants, function(property) {
                if (property.childrenLength) {
                    for (var i = 0; i < property.childrenLength; i++) {
                        importValues(sdUnitToImport, property, i, dicts, itemToImport);
                    }
                }
                importValues(sdUnitToImport, property, null, dicts, itemToImport);
            });

            return itemToImport;
        }

        return null;
    }

    function getPropertyCode(property, index) {
        if (_.isNull(index)) {
            return property.code;
        }

        return property.code + '_' + index;
    }

    function importItems(sdUnitsToImport, dicts) {
        var batches = _.map(sdUnitsToImport, function(unit) {
            var filled = getFilledProperties(unit, dicts);
            filled.structure = {molfile: unit.mol};

            return filled;
        });

        return batches;
    }

    function importFile() {
        close();
        dlg = $uibModal.open({
            animation: true,
            size: 'lg',
            template: template,
            controller: 'SingleFileUploaderController',
            controllerAs: 'vm',
            resolve: {
                url: function() {
                    return apiUrl + 'sd/import';
                }
            }
        });
        return dlg.result.then(function(result) {
            if (!result) {
                return $q.reject();
            }

            return dictionaryService.all({})
                .$promise
                .then(function(dicts) {
                    return importItems(result, dicts);
                });
        }, function() {
            alertModal.error('This file cannot be imported. Error occurred.');
        });
    }

    function close() {
        if (dlg) {
            dlg.close(null);
            dlg = null;
        }
    }
}

module.exports = sdImportService;
