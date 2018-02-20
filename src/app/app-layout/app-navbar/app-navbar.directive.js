var template = require('./app-navbar.html');
var roles = require('../../permissions/permission-roles.json');

function appNavbar() {
    return {
        restrict: 'E',
        controller: NavbarController,
        controllerAs: 'vm',
        template: template,
        bindToController: true,
        scope: {
            onToggleSidebar: '&'
        }
    };
}

NavbarController.$inject = ['$scope', '$state', 'principalService',
    'entitiesBrowserService', 'authService', 'entitiesCache'];

function NavbarController($scope, $state, principalService,
    entitiesBrowserService, authService, entitiesCache) {
    var vm = this;

    vm.GLOBAL_SEARCH = roles.GLOBAL_SEARCH;
    vm.logout = logout;
    vm.search = search;

    init();

    function init() {
        principalService.checkIdentity().then(function(user) {
            vm.user = user;
        });

        $scope.$on('$destroy', function() {
            entitiesCache.clearAll();
        });
    }

    function logout() {
        entitiesBrowserService.closeAllTabs()
        .then(() => {
            entitiesCache.clearAll();
            authService.logout();
        });
    }

    function search() {
        vm.query = (vm.query || '').trim().toLowerCase();
        $state.go('entities.search-panel', {query: vm.query});
    }
}

module.exports = appNavbar;
