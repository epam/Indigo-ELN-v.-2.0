(function() {
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
                indigoExperimentForm: '=',
                indigoSaveExperimentFn: '&'
            },
            link: link,
            templateUrl: 'scripts/components/entities/template/components/components-templete/components.html',
            controllerAs: 'vm',
            controller: function() {
                var vm = this;

                // for communication between components
                vm.share = {};
            }
        };

        /* @ngInject */
        function link(scope, element) {
            if (!scope.indigoExperiment) {
                return;
            }
            var id = scope.indigoExperiment.fullId;
            var tc;
            var preventFirstScroll;
            $timeout(function() {
                tc = element.find('.tab-content');
                if (scrollCache[id]) {
                    setTimeout(function() {
                        nostore = true;
                        tc[0].scrollTop = scrollCache[id];
                    }, 100);
                }
                var stimeout,
                    nostore;
                tc.on('scroll', function(e) {
                    // will close some dropdowns EPMLSOPELN-437
                    element.trigger('click');
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
                    stimeout = setTimeout(function() {
                        preventFirstScroll = true;
                    }, 300);
                    preventFirstScroll = false;
                    nostore = false;
                });
            }, 100);
        }
    }
})();
