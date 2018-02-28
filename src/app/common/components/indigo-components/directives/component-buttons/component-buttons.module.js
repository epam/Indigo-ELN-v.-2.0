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

require('./component-buttons.less');

var addNewBatch = require('./add-new-batch/add-new-batch.directive');
var deleteBatches = require('./delete-batches/delete-batches.directive');
var duplicateBatches = require('./duplicate-batches/duplicate-batches.directive');
var exportSdfFile = require('./export-sdf-file/export-sdf-file.directive');
var importSdfFile = require('./import-sdf-file/import-sdf-file.directive');
var syncWithIntendedProducts = require('./sync-with-intended-products/sync-with-intended-products.directive');
var registerBatches = require('./register-batches/register-batches.directive');

module.exports = angular
    .module('indigoeln.componentButtons', [])

    .directive('addNewBatch', addNewBatch)
    .directive('deleteBatches', deleteBatches)
    .directive('duplicateBatches', duplicateBatches)
    .directive('exportSdfFile', exportSdfFile)
    .directive('importSdfFile', importSdfFile)
    .directive('syncWithIntendedProducts', syncWithIntendedProducts)
    .directive('registerBatches', registerBatches)

    .name;
