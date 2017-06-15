angular.module('indigoeln')
    .factory('ProductBatchSummaryCache', function () {
        var _productBatchSummary;
        return {
            getProductBatchSummary: function () {
                return _productBatchSummary;
            },
            setProductBatchSummary: function (batches) {
                _productBatchSummary = batches;
            }
        };
    })
;