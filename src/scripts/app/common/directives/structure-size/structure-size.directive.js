require('./structure-size.less');
var template = require('./structure-size.html');

function stuctureSize() {
    return {
        restrict: 'E',
        scope: {
            model: '='
        },
        template: template,
        controller: angular.noop,
        controllerAs: 'vm',
        bindToController: true
    };
}

module.exports = stuctureSize;
