angular
    .module('indigoeln')
    .controller('CountdownDialogController', function($scope, countdown, idleTime) {
        var vm = this;

        $onInit();

        function $onInit() {
            vm.countdown = countdown;
            vm.countdownMax = countdown;
            vm.idleTime = idleTime;
        }
    });
