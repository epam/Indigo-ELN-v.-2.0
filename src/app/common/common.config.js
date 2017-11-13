/* @ngInject */
function commonConfig(uibPagerConfig, uibPaginationConfig) {
    // TODO: Looks like a global config -> app.config
    uibPagerConfig.itemsPerPage = 20;
    uibPagerConfig.previousText = '«';
    uibPagerConfig.nextText = '»';

    uibPaginationConfig.itemsPerPage = 20;
    uibPaginationConfig.maxSize = 5;
    uibPaginationConfig.boundaryLinks = true;
    uibPaginationConfig.firstText = '«';
    uibPaginationConfig.previousText = '‹';
    uibPaginationConfig.nextText = '›';
    uibPaginationConfig.lastText = '»';
}

module.exports = commonConfig;
