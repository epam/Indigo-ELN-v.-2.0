var template = require('./cell-typer.html');

function cellTyper() {
    return {
        restrict: 'E',
        template: template
    };
}

module.exports = cellTyper;

