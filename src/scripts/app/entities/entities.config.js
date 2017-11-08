var entitiesTemplate = require('./entities.html')
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
                    template: entitiesTemplate,
                    controller: 'EntitiesController',
                    controllerAs: 'vm'
                }
            }
        });
}

module.exports = entitiesConfig;
