(function(){

angular.module('indigoeln').directive('experimentTabs', experimentTabs);

experimentTabs.$inject = ['$timeout', '$filter'];

function experimentTabs($timeout, $filter) {
    return {
        scope : {},
        restrict : 'E',
        templateUrl : 'src/experimentTabs/experimentTabs.html',
        link : function(scope, elem, attrs) {
            scope.tabs = [];
            scope.newExperiment = newExperiment;
            
            scope.removeTab = removeTab;

            scope.$on('experiment-open', function(event, data) {
            	var openedTab = $filter('filter')(scope.tabs, {experiment : data.experiment});//-1;//scope.tabs.indexOf(data.experiment);
            	
            	if (openedTab.length > 0) {
            		openedTab[0].active = true;
            	} else {
                    scope.tabs.push({ experiment : data.experiment, active : true, content : 'src/experimentTabs/experimentTab/experimentTab.html', name : data.experiment.name});
                }
                
            });

            scope.$on('new-experiment', newExperiment);
            
            function newExperiment() {
                scope.tabs.push({active : true, content : 'src/experimentTabs/newExperimentTab/newExperimentTab.html', name : 'New experiment'});
            }

            function removeTab(tab) {
            	var indx = scope.tabs.indexOf(tab);
            	
            	scope.tabs.splice(indx, 1);            	
            }
        }
    }
}

})();