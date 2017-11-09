// region App styles
require('bootstrap-slider/dist/css/bootstrap-slider.min.css');
require('animate.css/animate.min.css');
require('font-awesome/css/font-awesome.min.css');
require('rdash-ui/dist/css/rdash.min.css');
// endregion

// region  External libs
require('./jquery');
require('./malihu-custom-scrollbar-plugin');
require('./jquery-ui');
require('bootstrap/dist/js/bootstrap.min');
require('angular');
// endregion

// region Base external modules
var uiRouter = require('angular-ui-router');
var ngResource = require('angular-resource');
var uiBootstrap = require('angular-ui-bootstrap');
var ngAnimate = require('angular-animate');
var ngIdle = require('ng-idle');
var ngCookies = require('angular-cookies');
var duScroll = require('angular-scroll');
var ngSanitize = require('angular-sanitize');
var uiTree = require('./angular-ui-tree');
var angularDragula = require('./angular-dragula');
var uiRouterExtras = require('./ui-router-extras');
var xeditable = require('./angular-xeditable');
var angularFileUpload = require('./angular-file-upload');
var ngFileSaver = require('./angular-file-saver');
var cgBusy = require('./angular-busy');
var angularFilter = require('./angular-filter');
var datePicker = require('./angular-datepicker');
var uiCheckbox = require('./angular-bootstrap-checkbox');
var monospacedElastic = require('./angular-elastic');
var uiBootstrapSlider = require('./angular-bootstrap-slider');
var angularCache = require('./angular-cache');
var uiSelect = require('./ui-select');
var cgNotify = require('./angular-notify');
// endregion

var vendors = [
    uiRouter,
    ngResource,
    uiTree,
    uiBootstrap,
    ngAnimate,
    ngIdle,
    ngCookies,
    duScroll,
    angularDragula,
    ngSanitize,
    uiRouterExtras,
    xeditable,
    angularFileUpload,
    ngFileSaver,
    cgBusy,
    angularFilter,
    datePicker,
    uiCheckbox,
    monospacedElastic,
    uiBootstrapSlider,
    angularCache,
    uiSelect,
    cgNotify
];

module.exports = vendors;
