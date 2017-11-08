var template = require('./simple-checkbox.html');

function simpleCheckbox() {
    return {
        restrict: 'E',
        scope: {
            model: '='
        },
        template: template
    };
}

module.exports = simpleCheckbox;
