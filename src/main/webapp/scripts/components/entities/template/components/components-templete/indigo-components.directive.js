(function () {
    angular
        .module('indigoeln')
        .directive('indigoComponents', indigoComponents);

    /* @ngInject */
    function indigoComponents($timeout) {
        var scrollCache = {};

        var bindings = {
            scrollCache: scrollCache,
            $timeout: $timeout
        };

        return {
            restrict: 'E',
            replace: true,
            scope: {
                indigoTemplate: '=',
                indigoReadonly: '=',
                indigoModel: '=',
                indigoExperiment: '=',
                indigoExperimentForm: '='
            },
            link: angular.bind(bindings, link),
            templateUrl: 'scripts/components/entities/template/components/components-templete/components.html'
        };
    }

    /* @ngInject */
    function link(scope, element) {
        var scrollCache = this.scrollCache,
            $timeout = this.$timeout;

        if (!scope.indigoExperiment) return;
        var id = scope.indigoExperiment.fullId, tc, preventFirstScroll;
        $timeout(function () {
            tc = element.find('.tab-content');
            if (scrollCache[id]) {
                setTimeout(function () {
                    nostore = true;
                    tc[0].scrollTop = scrollCache[id];
                }, 100);
            }
            var stimeout, nostore;
            tc.on('scroll', function (e) {
                if (nostore) {
                    nostore = false;
                    return;
                }
                if (!preventFirstScroll) {
                    scrollCache[id] = this.scrollTop;
                } else {
                    nostore = true;
                    tc[0].scrollTop = scrollCache[id] || 0;
                }
                clearTimeout(stimeout);
                stimeout = setTimeout(function () {
                    preventFirstScroll = true;
                }, 300);
                preventFirstScroll = false;
                nostore = false;
            });
        }, 100);

        //for communication between components
        scope.share = {};
    }
})();