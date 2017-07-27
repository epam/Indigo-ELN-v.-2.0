angular.module('indigoeln')
    .filter('containValue', function() {
        return function (targetObject, query, attributes) {
            var searchedAttributes = _.isArray(attributes) ? attributes : [attributes];
            var queryLowerCase = _.lowerCase(query);

            return _.isEmpty(query) || _.some(searchedAttributes, function(attribute) {
                return _.includes( _.lowerCase(targetObject[attribute]), queryLowerCase);
            });
        };
    });