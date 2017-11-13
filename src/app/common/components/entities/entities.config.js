entitiesConfig.$inject = ['$stateProvider'];

function entitiesConfig($stateProvider) {
    $stateProvider
        .state('entity', {
            abstract: true,
            parent: 'app_page'
        })
        .state('entities', {
            abstract: true,
            parent: 'entity',
            views: {
                'content@app_page': {
                    template: '<entities></entities>'
                }
            }
        });
}

module.exports = entitiesConfig;
