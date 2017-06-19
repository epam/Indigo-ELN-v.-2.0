angular
    .module('indigoeln')
    .factory('ProductBatchSummaryCache', ProductBatchSummaryCache);

/* @ngInject */
function ProductBatchSummaryCache() {
    var _productBatchSummary;

    return {
        getProductBatchSummary: getProductBatchSummary,
        setProductBatchSummary: setProductBatchSummary
    };

    function getProductBatchSummary () {
        return _productBatchSummary;
    }

    function setProductBatchSummary (batches) {
        _productBatchSummary = batches;
    }
}
