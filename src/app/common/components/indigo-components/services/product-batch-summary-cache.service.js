function productBatchSummaryCache() {
    var productBatchSummary;

    return {
        getProductBatchSummary: getProductBatchSummary,
        setProductBatchSummary: setProductBatchSummary
    };

    function getProductBatchSummary() {
        return productBatchSummary;
    }

    function setProductBatchSummary(batches) {
        productBatchSummary = batches;
    }
}

module.exports = productBatchSummaryCache;
