var template = require('./app-navbar.html');

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

NavbarController.$inject = ['$scope', '$state', 'principalService', 'authService', 'entitiesCacheService'];

function NavbarController($scope, $state, principalService, authService, entitiesCacheService) {
    var vm = this;

    vm.logout = logout;
    vm.search = search;

    init();

    function init() {
        principalService.checkIdentity().then(function(user) {
            vm.user = user;
        });

        $scope.$on('$destroy', function() {
            entitiesCacheService.clearAll();
        });
    }

    function logout() {
        authService.logout();
        entitiesCacheService.clearAll();
        $state.go('login');
    }

    function search() {
        vm.query = (vm.query || '').trim().toLowerCase();
        $state.go('entities.search-panel', {query: vm.query});
    }
}

module.exports = appNavbar;
