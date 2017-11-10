var template = require('./concept-details.html');

function indigoConceptDetails() {
    return {
        restrict: 'E',
        replace: true,
        template: template,
        controller: 'SomethingDetailsController',
        bindToController: true,
        controllerAs: 'vm',
        scope: {
            componentData: '=',
            experiment: '=',
            isReadonly: '='
        }
    };
}

module.exports = indigoConceptDetails;

