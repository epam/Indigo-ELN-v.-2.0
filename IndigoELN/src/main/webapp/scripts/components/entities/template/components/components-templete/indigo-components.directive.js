(function () {
    angular
        .module('indigoeln')
        .directive('indigoComponents', indigoComponents);

    /* @ngInject */
    function indigoComponents($timeout) {
        var scrollCache = {};

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
            link: link,
            templateUrl: 'scripts/components/entities/template/components/components-templete/components.html'
        };

        /* @ngInject */
        function link(scope, element) {
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
                    element.trigger('click'); // will close some dropdowns EPMLSOPELN-437
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
    }
})();