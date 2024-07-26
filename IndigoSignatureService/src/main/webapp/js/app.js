/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 *
 * This file is part of Indigo Signature Service.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
'use strict';

$.ajaxSetup({
    cache: false
});

var app = angular.module('App', ['App.controllers', 'App.directives', 'App.filters', 'App.services', 'ngRoute', 'ui.bootstrap', 'angularFileUpload', 'ngIdle', 'truncate', 'ngSanitize']);

app.config(['$routeProvider', '$locationProvider', function($routeProvider) {
    //$locationProvider.html5Mode(true);
    $routeProvider.when('/', {
        templateUrl: 'views/documentList.html',
        controller: 'documentListController'
    })
    .when('/templates', {
            templateUrl: 'views/templateList.html',
            controller: 'templateListController'
        })
    .when('/login', {
                templateUrl: 'views/login.html',
                controller: 'loginController'
            })
    .when('/users', {
                templateUrl: 'views/userList.html',
                controller: 'userListController'
            });

    $routeProvider.otherwise({ redirectTo: '/login' });


}])
.config(['$httpProvider',  function ($httpProvider) {
    $httpProvider.defaults.cache = false;

    if (!$httpProvider.defaults.headers.get) {
        $httpProvider.defaults.headers.get = {};
    }

    $httpProvider.defaults.headers.get['If-Modified-Since'] = 'Thu, 01 Jan 1970 00:00:00 GMT';
    $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
    $httpProvider.defaults.headers.get['Pragma'] = 'no-cache';

    $httpProvider.interceptors.push(function ($q, $rootScope) {
        return {
            'responseError': function (rejection) {
                if(rejection.status === 401) {
                    $rootScope.$broadcast('event:loginRequired');
                }
                return $q.reject(rejection);
            }
        };
    });
}])
.config(['$keepaliveProvider', function ($keepaliveProvider) {
    $keepaliveProvider.interval(900);
}])
.controller('KeepAliveCtrl', ['$rootScope', '$http', function($rootScope, $http) {
    $rootScope.$on('$keepalive', function(event, data) {
        $http.get('getUser');
    });
}])
.run(function($controller, $keepalive) {
    $controller('KeepAliveCtrl');
    $keepalive.start();
});
