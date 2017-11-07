var template = require('./reaction-details.html');

function indigoReactionDetails() {
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

module.export = indigoReactionDetails;

