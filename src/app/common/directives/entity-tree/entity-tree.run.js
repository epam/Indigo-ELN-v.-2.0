var sidebarPopoverTemplate = require('./popovers/experiment-detail-popover.html');

run.$inject = ['entityTreeService', 'principalService', '$templateCache'];

function run(entityTreeService, principalService, $templateCache) {
    principalService.addUserChangeListener(entityTreeService.clearAll);

    $templateCache.put('experiment-detail-popover.html', sidebarPopoverTemplate);
}

module.exports = run;
