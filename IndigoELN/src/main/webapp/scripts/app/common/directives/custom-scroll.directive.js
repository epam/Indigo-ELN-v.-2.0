(function() {
    angular
        .module('indigoeln')
        .directive('customScroll', customScroll);

    /* @ngInject */
    function customScroll($state, Principal, simpleLocalCache, $timeout) {
        return {
            restrict: 'A',
            link: link
        };

        /* @ngInject */
        function link($scope, $element, attributes) {
            Principal.identity()
                .then(function(user) {
                    var currentTab = attributes["customScroll"];
                    var key = user.id + "." + $state.params.projectId + '-' + $state.params.notebookId + '-' + $state.params.experimentId + '.' + currentTab + '.scrollPos';
                    var scrollLocation = simpleLocalCache.getByKey(key) || 0;

                    $timeout(function () {
                        $element.scrollTop(scrollLocation);
                    });

                    $element.on('scroll', function() {
                        simpleLocalCache.putByKey(key, $element[0].scrollTop);
                    });
                });
        }
    }
})();
