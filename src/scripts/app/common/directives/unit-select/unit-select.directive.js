var template = require('./unit-select.html');

function unitSelect() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            unit: '=',
            units: '=',
            appendToBody: '=',
            onChange: '&'
        },
        controller: UnitSelectController,
        controllerAs: 'vm',
        bindToController: true
    };

    function UnitSelectController() {
        var vm = this;

        init();

        function init() {
            vm.isOpen = false;
        }
    }
}

module.export = unitSelect;
