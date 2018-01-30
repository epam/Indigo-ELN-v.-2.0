var template = require('./set-scalar-value.html');
var controller = require('./set-scalar-value.controller');

scalarService.$inject = ['$uibModal', 'registrationUtil', 'calculationService'];

function scalarService($uibModal, registrationUtil, calculationService) {
    return {
        action: action
    };

    function action(rows, title, column) {
        return $uibModal.open({
            template: template,
            controller: controller,
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                name: function() {
                    return title;
                }
            }
        }).result.then(function(result) {
            return _.reduce(rows, function(array, row) {
                if (!registrationUtil.isRegistered(row)) {
                    row[column.id].value = result;
                    row[column.id].entered = true;
                    if (column.id === 'saltEq') {
                        array.push(calculationService.recalculateSalt(row));
                    }
                }

                return array;
            }, []);
        });
    }
}

module.exports = scalarService;
