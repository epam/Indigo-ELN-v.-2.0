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

var EditExternalSupplierController = require('./edit-external-supplier/edit-external-supplier.controller');
var EditMeltingPointController = require('./edit-melting-point/edit-melting-point.controller');
var EditPurityController = require('./edit-purity/edit-purity.controller');
var EditResidualSolventsController = require('./edit-residual-solvents/edit-residual-solvents.controller');
var EditSolubilityController = require('./edit-solubility/edit-solubility.controller');
var SelectFromDictionaryController = require('./select-from-dictionary/select-from-dictionary.controller');

var infoEditorService = require('./info-editor.service');

module.exports = angular
    .module('indigoeln.editInfoPopup', [])

    .controller('EditExternalSupplierController', EditExternalSupplierController)
    .controller('EditMeltingPointController', EditMeltingPointController)
    .controller('EditPurityController', EditPurityController)
    .controller('EditResidualSolventsController', EditResidualSolventsController)
    .controller('EditSolubilityController', EditSolubilityController)
    .controller('SelectFromDictionaryController', SelectFromDictionaryController)

    .factory('infoEditorService', infoEditorService)

    .name;
