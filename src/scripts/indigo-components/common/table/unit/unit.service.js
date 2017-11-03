angular
    .module('indigoeln.componentsModule')
    .factory('unitService', unitService);

/* @ngInject */
function unitService($uibModal, calculationService, registrationUtil) {
    return {
        getActions: function(name, unitItems) {
            var actions = [{
                name: 'Set value for ' + name,
                title: name,
                units: unitItems,
                action: function(rows, column) {
                    unitAction(rows, name, column, unitItems);
                }
            }];
            _.map(unitItems, function(unit) {
                actions.push(toUnitAction(unit));
            });

            return actions;
        }
    };

    function unitAction(rows, title, column, units) {
        var id = column.id;

        return $uibModal.open({
            templateUrl: 'scripts/indigo-components/common/table/unit/set-unit-value.html',
            controller: 'SetUnitValueController',
            controllerAs: 'vm',
            size: 'sm',
            resolve: {
                name: function() {
                    return title;
                },
                unitNames: function() {
                    return units;
                }
            }
        }).result.then(function(result) {
            _.each(rows, function(row) {
                if (!registrationUtil.isRegistered(row)) {
                    row[id] = row[id] || {};
                    row[id].value = result.value;
                    row[id].unit = result.unit;
                    row[id].entered = true;
                    calculationService.calculateProductBatch({
                        row: row, column: id
                    });
                }
            });
        });
    }

    function setUnit(name, item) {
        item.unit = name;
    }

    function toUnitAction(unit) {
        return {
            name: 'Set Unit ' + unit,
            action: function(rows, column) {
                _.each(rows, function(row) {
                    setUnit(unit, row[column.id]);
                });
            }
        };
    }
}
