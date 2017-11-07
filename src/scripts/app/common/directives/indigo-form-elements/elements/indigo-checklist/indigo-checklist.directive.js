var template = require('./indigo-checklist.html');

function indigoChecklist() {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            indigoItems: '=',
            indigoLabel: '@'
        },
        controller: IndigoChecklistController,
        controllerAs: 'vm',
        bindToController: true,
        templateUrl: template
    };

    /* @ngInject */
    function IndigoChecklistController() {
        var vm = this;
        vm.allItemsSelected = false;

        vm.allChanged = function() {
            _.each(vm.indigoItems, function(item) {
                item.isChecked = vm.allItemsSelected;
            });
        };
    }
}

module.export = indigoChecklist;


