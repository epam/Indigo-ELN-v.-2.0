/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

// region App styles
require('bootstrap-slider/dist/css/bootstrap-slider.min.css');
require('animate.css/animate.min.css');
require('rdash-ui/dist/css/rdash.min.css');
// endregion

// region  External libs
require('expose-loader?$!expose-loader?jQuery!jquery');
require('expose-loader?angular!angular');
require('expose-loader?_!lodash');
require('./jquery-ui');
require('bootstrap/dist/js/bootstrap.min');
require('angular');
require('expose-loader?moment!moment-timezone');
require('sockjs-client');
require('./simditor');
require('pretty-bytes');
require('./perfect-scrollbar');
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

module.exports = angular
    .module('indigoeln.vendors', vendors)

    .name;
