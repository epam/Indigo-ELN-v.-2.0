(function(){

angular.module('indigoeln').directive('experimentTabs', experimentTabs);

experimentTabs.$inject = ['$timeout'];

function experimentTabs($timeout) {
    return {
        scope : {},
        restrict : 'E',
        templateUrl : 'views/experimentTabs/experimentTabs.html',
        link : function(scope, elem, attrs) {
            scope.experiments = [];
            scope.selectedIndex = 0;

            scope.$on('experiment-open', function(event, data) {
            	var index = scope.experiments.indexOf(data.experiment);
            	
            	if (index != - 1) {
            		scope.selectedIndex = index + 1;//+1 cause of welcome page
            	} else {
                    scope.experiments.push(data.experiment);
                    $timeout(function() {
                        scope.selectedIndex = scope.experiments.length - 1 + 1;
                    });
                }
            });
        }
    }
}

})();