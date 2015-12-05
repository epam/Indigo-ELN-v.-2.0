(function(){

angular.module('indigoeln').directive('experimentTree', experimentTree);

experimentTree.$inject = ['experimentService', '$rootScope'];

function experimentTree(experimentService, $rootScope) {
    return {
    	scope : {},
        restrict : 'E',
        templateUrl : 'views/experimentTree/experimentTree.html',
        link : function(scope, elem, attrs) {
        	scope.data = [{ 
        	   name : 'Experiments',
        	   type : 'root',
        	   nodes : experimentService.query()
        	}];
        	
        	scope.open = function(data) {
        	   $rootScope.$broadcast('experiment-open', {experiment : data})
        	}
        	
        	scope.$on('new-experiment', function(event, data) {
        	   scope.data[0].nodes.push(data.experiment);
        	});
        }
    }
}

})();