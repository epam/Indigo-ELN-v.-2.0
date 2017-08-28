angular.module('indigoeln')
    .filter('selectedItemCounter', selectedItemCounter);

function selectedItemCounter() {
    return function(list, field) {
        if (!field) {
            field = '$$isSelected';
        }
        var filteringObject = {};
        filteringObject[field] = true;

        return _.filter(list, filteringObject);
    };
}
