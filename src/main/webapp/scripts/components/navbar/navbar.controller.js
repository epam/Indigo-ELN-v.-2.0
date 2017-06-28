(function() {
    angular
        .module('indigoeln')
        .controller('NavbarController', NavbarController);

    /* @ngInject */
    function NavbarController($scope, $state, $rootScope, Principal, Auth, EntitiesBrowser, EntitiesCache) {
        var vm = this;

        vm.logout = logout;
        vm.onSearchKeyup = onSearchKeyup;
        vm.search = search;

        init();

        function init() {
            Principal.identity().then(function(user) {
                vm.user = user;
            });

            // TODO: Do we really need this watcher?
            var unsubscribe = $scope.$watch(function() {
                return EntitiesBrowser.getActiveTab();
            }, function(value) {
                $scope.activeTab = value;
            });

            $scope.$on('$destroy', function() {
                EntitiesCache.clearAll();
                unsubscribe();
            });
        }

        function logout() {
            var userId = Principal.getIdentity().id;
            Auth.logout();
            EntitiesCache.clearAll();
            $rootScope.$broadcast('user-logout', {
                id: userId
            });
            $state.go('login');
        }

        function onSearchKeyup(e) {
            if (e.keyCode === 13) {
                vm.search();
                e.preventDefault();
            }
        }

        function search() {
            vm.query = vm.query.trim().toLowerCase();
            $state.go('entities.search-panel').then(function() {
                $rootScope.$broadcast('toggle-search', {
                    query: vm.query
                });
            });
        }
    }
})();
