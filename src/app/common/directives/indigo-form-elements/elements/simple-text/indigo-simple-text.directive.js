var template = require('./simple-text.html');

function indigoSimpleText() {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            indigoLabel: '@',
            indigoModel: '=',
            indigoEmptyText: '@',
            indigoClasses: '@'
        },
        template: template
    };
}

module.exports = indigoSimpleText;
