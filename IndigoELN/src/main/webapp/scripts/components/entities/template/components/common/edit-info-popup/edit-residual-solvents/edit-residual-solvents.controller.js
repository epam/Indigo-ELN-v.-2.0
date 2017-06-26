angular
    .module('indigoeln')
    .controller('editResidualSolventsController', editResidualSolventsController);

/* @ngInject */
function editResidualSolventsController($scope, $uibModalInstance, data) {
    var vm = this;

    init();

    function init() {
        vm.solvents = data || {};
        vm.solvents.data = vm.solvents.data || [];

        vm.save = save;
        vm.cancel = cancel;
        vm.addSolvent = addSolvent;
        vm.remove = remove;
        vm.removeAll = removeAll;
    }

    function addSolvent() {
        $scope.solvents.data.push({name: '', eq: '', comment: ''});
    }

    function remove(solvent) {
        $scope.solvents.data = _.without($scope.solvents.data, solvent);
    }

    function removeAll() {
        $scope.solvents.data = [];
    }

    function resultToString() {
        var solventStrings = _.map($scope.solvents.data, function(solvent) {
            if (solvent.name && solvent.eq) {
                return solvent.eq + ' mols of ' + solvent.name.name;
            }

            return '';
        });

        return _.compact(solventStrings).join(', ');
    }

    function save() {
        $scope.solvents.asString = resultToString();
        $uibModalInstance.close($scope.solvents);
    }

    function cancel() {
        $uibModalInstance.dismiss('cancel');
    }
}
