require('./app-navbar.less');
var appNavbar = require('./app-navbar.directive');

module.exports = angular
    .module('indigoeln.appNavbar', [])

    .directive('appNavbar', appNavbar)

    .name;
