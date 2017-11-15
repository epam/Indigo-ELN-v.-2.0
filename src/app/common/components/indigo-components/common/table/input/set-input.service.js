var template = require('./set-input-value.html');

setInputService.$inject = ['$uibModal', 'registrationUtilService'];

function setInputService($uibModal, registrationUtilService) {
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
            template: template,
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
                if (!registrationUtilService.isRegistered(row)) {
                    row[columnId] = result;
                }
            });
        });
    }
}

module.exports = setInputService;
