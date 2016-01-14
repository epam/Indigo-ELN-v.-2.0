'use strict';

angular
    .module('indigoeln')
    .controller('SidebarController', SidebarController);

SidebarController.$inject = ['$location', '$state', 'authService'];

function SidebarController($location, $state, authService) {
    var vm = this;
    //vm.isAuthenticated = authService.isAuthenticated();
}