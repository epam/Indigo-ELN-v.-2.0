(function() {
    angular
        .module('indigoeln')
        .directive('pressEnter', pressEnter);

    function pressEnter() {
        return {
            restrict: 'A',
            scope: {
                pressEnter: "="
            },
            link: function($scope, element){
                element.on('keypress', function($event) {
                    if ($event.keyCode === 13) {
                        $event.preventDefault();
                        $scope.pressEnter();
                    }
                });
            }
        };
    }
})();

