/* @ngInject */
function CountdownDialogController($scope, countdown, idleTime) {
    var vm = this;

    $onInit();

    function $onInit() {
        vm.countdown = countdown;
        vm.countdownMax = countdown;
        vm.idleTime = idleTime;
    }
}

module.exports = CountdownDialogController;
