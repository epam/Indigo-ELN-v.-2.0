var template = require('./set-select-value.html');

selectService.$inject = ['$uibModal', 'registrationUtil'];

function selectService($uibModal, registrationUtil) {
    return {
        getActions: function(name, values, dictionary) {
            return [{
                name: 'Set value for ' + name,
                title: name,
                values: values,
                dictionary: dictionary,
                action: function(rows, column) {
                    action(rows, name, column.id, dictionary, values);
                }
            }];
        }
    };

    function action(rows, title, columnId, dictionary, values) {
        $uibModal.open({
            template: template,
            controller: 'SetSelectValueController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                id: function() {
                    return columnId;
                },
                name: function() {
                    return title;
                },
                values: function() {
                    return values;
                },
                dictionary: function() {
                    return dictionary;
                }
            }
        }).result.then(function(result) {
            _.each(rows, function(row) {
                if (!registrationUtil.isRegistered(row)) {
                    row[columnId] = result;
                }
            });
        });
    }
}
