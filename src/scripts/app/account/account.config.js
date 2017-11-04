/* @ngInject */
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
                    template: require('./login/login.html'),
                    controller: 'LoginController',
                    controllerAs: 'vm'
                }
            },
            resolve: {}
        });
}

module.exports = accountConfig;
