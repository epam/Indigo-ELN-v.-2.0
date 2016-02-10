'use strict';

angular
    .module('indigoeln')
    .controller('CountdownDialogController', function ($scope, countdown, idleTime) {
        $scope.countdown = countdown;
        $scope.countdownMax = countdown;
        $scope.idleTime = idleTime;
    });