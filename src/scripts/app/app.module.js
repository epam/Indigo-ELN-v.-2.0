/**
 * External libs
 */
require('./dependencies/jquery');
require('./dependencies/malihu-custom-scrollbar-plugin');
require('./dependencies/jquery-ui');
require('bootstrap/dist/js/bootstrap.min');

/**
 * Base external modules
 */
var angular = require('angular');
var uiRouter = require('angular-ui-router');
var ngResource = require('angular-resource');
var uiTree = require('./dependencies/angular-ui-tree');
var uiBootstrap = require('angular-ui-bootstrap');
var ngAnimate = require('angular-animate');
var ngIdle = require('ng-idle');
var ngSanitize = require('angular-sanitize');
var ngCookies = require('angular-cookies');
var duScroll = require('angular-scroll');

var angularDragula = require('./dependencies/angular-dragula');
var uiRouterExtras = require('./dependencies/ui-router-extras');
var xeditable = require('./dependencies/angular-xeditable');
var angularFileUpload = require('./dependencies/angular-file-upload');
var ngFileSaver = require('./dependencies/angular-file-saver');
var cgBusy = require('./dependencies/angular-busy');
var angularFilter = require('./dependencies/angular-filter');
var datePicker = require('./dependencies/angular-datepicker');
var uiCheckbox = require('./dependencies/angular-bootstrap-checkbox');
var monospacedElastic = require('./dependencies/angular-elastic');
var uiBootstrapSlider = require('./dependencies/angular-bootstrap-slider');
var angularCache = require('./dependencies/angular-cache');
var uiSelect = require('./dependencies/ui-select');
var cgNotify = require('./dependencies/angular-notify');

/**
 * App styles
 */
require('../../assets/less/indigo-bootstrap.less');
require('bootstrap-slider/dist/css/bootstrap-slider.min.css');
require('animate.css/animate.min.css');
require('font-awesome/css/font-awesome.min.css');
require('rdash-ui/dist/css/rdash.min.css');
require('../../assets/less/main.less');

/**
 * App modules
 */
var account = require('./account/account.module');
var appLayout = require('./app-layout/app-layout.module');
var appNavbar = require('./app-navbar/app-navbar.module');
var common = require('./common/common.module');
var dictionaryManagement = require('./dictionary-management/dictionary-management.module');
var entities = require('./entities/entities.module');
var permissions = require('./permissions/permissions.module');
var project = require('./project/project.module');
var sidebar = require('./sidebar/sidebar.module');
var userManagement = require('./user-management/user-management.module');

var appRun = require('./app.run');
var appConfig = require('./app.config');

var dependencies = [
    uiRouter,
    ngResource,
    uiTree,
    uiBootstrap,
    ngAnimate,
    ngIdle,
    uiRouterExtras,
    xeditable,
    angularFileUpload,
    ngCookies,
    angularDragula,
    cgBusy,
    angularFilter,
    ngFileSaver,
    uiSelect,
    ngSanitize,
    datePicker,
    uiCheckbox,
    monospacedElastic,
    uiBootstrapSlider,
    angularCache,
    cgNotify,
    duScroll,
    account,
    appLayout,
    appNavbar,
    common,
    dictionaryManagement,
    entities,
    permissions,
    project,
    sidebar,
    userManagement
];

module.exports = angular
    .module('indigoeln', dependencies)

    .run(appRun)
    .config(appConfig)

    .constant('apiUrl', apiUrl)

    .name;
