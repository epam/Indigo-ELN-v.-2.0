(function() {
    angular
        .module('indigoeln')
        .directive('navbar', function() {
            return {
                restrict: 'E',
                controller: NavbarController,
                controllerAs: 'vm',
                templateUrl: 'scripts/components/navbar/navbar.html',
                bindToController: true,
                scope: {
                    onToggleSidebar: '&'
                }
            };
        });

    /* @ngInject */
    function NavbarController($scope, $state, Principal, Auth, EntitiesCache) {
        var vm = this;

        vm.logout = logout;
        vm.search = search;

        init();

        function init() {
            Principal.identity().then(function(user) {
                vm.user = user;
            });

            $scope.$on('$destroy', function() {
                EntitiesCache.clearAll();
            });
        }

        function logout() {
            Auth.logout();
            EntitiesCache.clearAll();
            $state.go('login');
        }

        function search() {
            vm.query = (vm.query || '').trim().toLowerCase();
            $state.go('entities.search-panel', {query: vm.query});
        }
    }
})();
