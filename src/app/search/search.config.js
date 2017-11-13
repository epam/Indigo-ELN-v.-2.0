var template = require('./component/search-panel.html');

searchConfig.$inject = ['$stateProvider'];

function searchConfig($stateProvider) {
    $stateProvider
        .state('entities.search-panel', {
            url: '/search',
            params: {query: null},
            views: {
                tabContent: {
                    template: template,
                    controller: 'SearchPanelController',
                    controllerAs: 'vm'
                }
            },
            data: {
                pageTitle: 'indigoeln',
                tab: {
                    name: 'Search',
                    kind: 'search-panel',
                    type: 'entity',
                    state: 'entities.search-panel',
                    isSingle: true
                }
            },
            resolve: {
                pageInfo: function($q, principalService, usersService) {
                    return $q.all([
                        usersService.get(),
                        principalService.checkIdentity()
                    ]).then(function(results) {
                        return {
                            users: results[0],
                            identity: results[1]
                        };
                    });
                }
            }
        });
}

module.exports = searchConfig;
