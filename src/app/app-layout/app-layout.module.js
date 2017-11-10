var appLayout = require('./component/app-layout.directive');

var appNavbar = require('./app-navbar/app-navbar.module');
var appSidebar = require('./app-sidebar/app-sidebar.module');

var dependencies = [
    appNavbar,
    appSidebar
];

module.exports = angular
    .module('indigoeln.appLayout', dependencies)

    .directive('appLayout', appLayout)

    .name;
