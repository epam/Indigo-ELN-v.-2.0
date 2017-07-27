angular.module('indigoeln')
    .filter('searchComponent', function() {
        return function (component, string) {
            var stringLowerCase = _.lowerCase(string);
            var nameLowerCase = _.lowerCase(component.name);
            var descLowerCase =_.lowerCase(component.desc);

            return _.includes(nameLowerCase, stringLowerCase ) || _.includes(descLowerCase, stringLowerCase );
        };
    });