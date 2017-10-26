(function() {
    angular
        .module('indigoeln')
        .directive('indigoFileReader', indigoFileReader);

    /* @ngInject */
    function indigoFileReader($parse) {
        return {
            restrict: 'A',
            scope: false,
            controller: controller,
            link: link
        };

        /* @ngInject */
        function link($scope, $element, $attrs) {
            var showFunc = $parse($attrs.indigoFileReader);

            $element.on('change', function(onChangeEvent) {
                var reader = new FileReader();
                reader.onload = function(onLoadEvent) {
                    $scope.$apply(function() {
                        showFunc($scope, {
                            $fileContent: onLoadEvent.target.result
                        });
                    });
                };
                reader.readAsText((onChangeEvent.srcElement || onChangeEvent.target).files[0]);
            });
        }

        /* @ngInject */
        function controller($scope) {
            $scope.showContent = function($fileContent) {
                $scope.content = $fileContent;
            };
        }
    }
})();
