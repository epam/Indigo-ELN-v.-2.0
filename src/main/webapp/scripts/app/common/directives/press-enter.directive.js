(function() {
    angular
        .module('indigoeln')
        .directive('pressEnter', pressEnter);

    function pressEnter() {
        return {
            restrict: 'A',
            link: function($scope, element, attrs){
                element.on('keypress', function($event) {
                    if ($event.keyCode === 13) {
                        $scope.$apply(function(){
                            $scope.$eval(attrs.pressEnter, {'$event': $event});
                        });
                    }
                });
            }
        };
    }
})();

