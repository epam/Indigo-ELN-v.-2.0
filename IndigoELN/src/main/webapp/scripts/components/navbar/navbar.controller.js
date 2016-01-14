'use strict';

angular
    .module('indigoeln')
    .controller('NavbarController', NavbarController);

NavbarController.$inject = ['$location', '$state', 'authService'];

function NavbarController($location, $state, authService) {
    var vm = this;
    //vm.isAuthenticated = authService.isAuthenticated();
}