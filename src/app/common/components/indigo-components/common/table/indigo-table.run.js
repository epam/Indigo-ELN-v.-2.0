var imagePopoverTemplate = require('./image-popover.html');

run.$inject = ['$templateCache'];

function run($templateCache) {
    $templateCache.put('image-popover.html', imagePopoverTemplate);
}

module.exports = run;
