(function () {
    angular
        .module('indigoeln')
        .controller('LoginController', LoginController);

    /* @ngInject */
    function LoginController($state, Auth, Principal) {
        var vm = this;

        vm.user = {};
        vm.errors = {};
        vm.rememberMe = true;

        vm.login = login;

        function login(event) {
            vm.loading = true;
            event.preventDefault();

            Auth.login({
                username: vm.username,
                password: vm.password,
                rememberMe: vm.rememberMe
            }).then(function () {
                vm.authenticationError = false;
                Principal.identity(true).then(function () {
                    $state.go('experiment');
                    vm.loading = false;
                });
            }).catch(function () {
                vm.authenticationError = true;
                vm.shake = !vm.shake;
                vm.loading = false;
            });
        }
    }
})();
