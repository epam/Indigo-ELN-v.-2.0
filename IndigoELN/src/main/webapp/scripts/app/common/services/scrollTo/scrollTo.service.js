angular
    .module('indigoeln')
    .factory('scrollService', scrollService);

/* @ngInject */
function scrollService(smoothScroll) {
    var defaultOptions = {
        duration: 300,
        easing: 'easeInQuad',
        offset: 0
    };

    return {
        scrollTo: function(selector, options) {
            var element = angular.element(selector);
            if (element.length) {
                smoothScroll(angular.element(selector)[0], _.extend({}, defaultOptions, options));
            }
        }
    };
}
