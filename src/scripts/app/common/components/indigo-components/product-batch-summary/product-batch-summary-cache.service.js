angular
    .module('indigoeln.componentsModule')
    .factory('productBatchSummaryCache', productBatchSummaryCache);

/* @ngInject */
function productBatchSummaryCache() {
    var _productBatchSummary;

    return {
        getProductBatchSummary: getProductBatchSummary,
        setProductBatchSummary: setProductBatchSummary
    };

    function getProductBatchSummary() {
        return _productBatchSummary;
    }

    function setProductBatchSummary(batches) {
        _productBatchSummary = batches;
    }
}
