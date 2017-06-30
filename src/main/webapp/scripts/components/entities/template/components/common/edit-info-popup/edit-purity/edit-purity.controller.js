angular
    .module('indigoeln')
    .controller('EditPurityController', EditPurityController);

/* @ngInject */
function EditPurityController($scope, $uibModalInstance, data, dictionary) {
    var vm = this;

    init();

    function init() {

        console.log($scope);
        vm.purity = data || {};
        vm.purity.data = vm.purity.data || [];
        vm.dictionary = dictionary;

        vm.operatorSelect = [
            {
                name: '>'
            },
            {
                name: '<'
            },
            {
                name: '='
            },
            {
                name: '~'
            }];

        vm.unknownPurity = 'Purity Unknown';

        vm.isInknownPurity = isInknownPurity;
        vm.save = save;
        vm.cancel = cancel;
    }

    function isInknownPurity() {
        return vm.purity.property === vm.unknownPurity;
    }

    function resultToString() {
        var purityStrings = _.map(vm.purity.data, function(purity) {
            if (purity.operator && purity.value && purity.comments) {
                return purity.determinedBy + ' purity ' + purity.operator.name + ' ' +
                    purity.value + '% ' + purity.comments;
            } else if (purity.operator && purity.value) {
                return purity.determinedBy + ' purity ' + purity.operator.name + ' ' +
                    purity.value + '%';
            }

            return '';
        });

        return _.compact(purityStrings).join(', ');
    }

    function save() {
        if (vm.isInknownPurity()) {
            vm.purity = {};
            vm.purity.asString = vm.unknownPurity;
        } else {
            vm.purity.asString = resultToString();
        }
        $uibModalInstance.close(vm.purity);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}
