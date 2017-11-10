var template = require('./login/login.html');

accountConfig.$inject = ['$stateProvider'];

function accountConfig($stateProvider) {
    $stateProvider
        .state('account', {
            abstract: true
        })
        .state('login', {
            parent: 'account',
            url: '/login',
            data: {
                authorities: [],
                pageTitle: 'Sign in'
            },
            views: {
                'app_page@': {
                    template: template,
                    controller: 'LoginController',
                    controllerAs: 'vm'
                }
            },
            resolve: {}
        });
}

module.exports = accountConfig;
