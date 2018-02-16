/* @ngInject */
function CountdownDialogController(countdown, idleTime) {
    var vm = this;

    vm.countdown = countdown;
    vm.countdownMax = countdown;
    vm.idleTime = idleTime;
}

module.exports = CountdownDialogController;
