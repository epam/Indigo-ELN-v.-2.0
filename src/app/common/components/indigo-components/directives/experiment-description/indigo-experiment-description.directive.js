var template = require('./experiment-description.html');

function indigoExperimentDescription() {
    return {
        restrict: 'E',
        replace: true,
        template: template,
        scope: {
            model: '=',
            experiment: '=',
            isReadonly: '=',
            onChanged: '&'
        },
        bindToController: true,
        controllerAs: 'vm',
        controller: angular.noop
    };
}

module.exports = indigoExperimentDescription;
