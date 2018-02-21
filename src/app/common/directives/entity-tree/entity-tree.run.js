var sidebarPopoverTemplate = require('./popovers/experiment-detail-popover.html');

/* @ngInject */
function entityTreeRun(entityTreeService, principalService, $templateCache) {
    principalService.addUserChangeListener(entityTreeService.clearAll);

    $templateCache.put('experiment-detail-popover.html', sidebarPopoverTemplate);
}

module.exports = entityTreeRun;
