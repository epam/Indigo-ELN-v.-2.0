require('./columns-settings.less');

var template = require('./columns-settings.html');

function columnsSettings() {
    return {
        restrict: 'E',
        template: template,
        scope: {
            columns: '=',
            visibleColumns: '=',
            onChanged: '&',
            resetColumns: '&'
        },
        controller: angular.noop,
        controllerAs: 'vm',
        bindToController: true
    };
}

module.exports = columnsSettings;
