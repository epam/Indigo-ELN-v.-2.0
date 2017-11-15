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
