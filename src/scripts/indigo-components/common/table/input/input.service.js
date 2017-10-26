angular
    .module('indigoeln.Components')
    .factory('inputService', inputService);

/* @ngInject */
function inputService($uibModal, RegistrationUtil) {
    return {
        getActions: function(name) {
            return [{
                name: 'Set value for ' + name,
                title: name,
                action: function(rows, column) {
                    action(rows, name, column.id);
                }
            }];
        }
    };

    function action(rows, title, columnId) {
        $uibModal.open({
            templateUrl: 'scripts/indigo-components/common/table/input/set-input-value.html',
            controller: 'SetInputValueController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                name: function() {
                    return title;
                }
            }
        }).result.then(function(result) {
            _.each(rows, function(row) {
                if (!RegistrationUtil.isRegistered(row)) {
                    row[columnId] = result;
                }
            });
        });
    }
}
