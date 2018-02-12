/* @ngInject */
function scrollToService() {
    return {
        scrollTo: scrollTo
    };

    function scrollTo(selector, options) {
        var parent = options.container;
        if (parent) {
            var target = parent.find(selector);
            if (target.length) {
                var offset = (parent.height() / 2) - target.height();
                parent.scrollToElement(target, offset, 0);
            }
        }
    }
}

module.exports = scrollToService;
