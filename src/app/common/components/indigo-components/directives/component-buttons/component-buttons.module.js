require('./component-buttons.less');

var addNewBatchDirective = require('./add-new-batch/add-new-batch');
var deleteBatchesDirective = require('./delete-batches/delete-batches');
var duplicateBatchesDirective = require('./duplicate-batches/duplicate-batches');
var exportSdfFileDirective = require('./export-sdf-file/export-sdf-file');
var importSdfFileDirective = require('./import-sdf-file/import-sdf-file');
var syncWithIntendedProducts = require('./sync-with-intended-products/sync-with-intended-products');

module.exports = angular
    .module('indigoeln.componentButtons', [])

    .directive('addNewBatch', addNewBatchDirective)
    .directive('deleteBatches', deleteBatchesDirective)
    .directive('duplicateBatches', duplicateBatchesDirective)
    .directive('exportSdfFile', exportSdfFileDirective)
    .directive('importSdfFile', importSdfFileDirective)
    .directive('syncWithIntendedProducts', syncWithIntendedProducts)

    .name;
