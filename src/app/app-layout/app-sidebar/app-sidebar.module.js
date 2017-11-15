require('./component/sidebar.less');

var sidebar = require('./component/sidebar.directive');
var allExperimentsService = require('./resources/all-experiments.service');
var allNotebooksService = require('./resources/all-notebooks.service');
var allProjectsService = require('./resources/all-projects.service');
var sidebarCacheService = require('./services/sidebar-cache.service');

module.exports = angular
    .module('indigoeln.appSidebar', [])

    .directive('sidebar', sidebar)

    .factory('allExperimentsService', allExperimentsService)
    .factory('allNotebooksService', allNotebooksService)
    .factory('allProjectsService', allProjectsService)
    .factory('sidebarCacheService', sidebarCacheService)

    .name;
