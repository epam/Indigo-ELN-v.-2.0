var errorHandlerInterceptor = require('./error-handler.interceptor');
var notificationInterceptor = require('./notification.interceptor');

module.exports = angular
    .module('indigoeln.common.interceptors', [])

    .factory('errorHandlerInterceptor', errorHandlerInterceptor)
    .factory('notificationInterceptor', notificationInterceptor)

    .name;
