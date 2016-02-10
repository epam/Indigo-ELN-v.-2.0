'use strict';

angular
    .module('indigoeln')
    .controller('CountdownDialogController', function ($scope, countdown) {
        $scope.countdown = countdown;
        $scope.countdownMax = countdown;
    });