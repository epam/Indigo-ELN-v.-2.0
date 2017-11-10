require('./component/sidebar.less');

var sidebar = require('./component/sidebar.directive');
var allExperiments = require('./resources/all-experiments.service');
var allNotebooks = require('./resources/all-notebooks.service');
var allProjects = require('./resources/all-projects.service');
var sidebarCache = require('./services/sidebar-cache.service');

module.exports = angular
    .module('indigoeln.appSidebar', [])

    .directive('sidebar', sidebar)

    .factory('allExperiments', allExperiments)
    .factory('allNotebooks', allNotebooks)
    .factory('allProjects', allProjects)
    .factory('sidebarCache', sidebarCache)

    .name;
