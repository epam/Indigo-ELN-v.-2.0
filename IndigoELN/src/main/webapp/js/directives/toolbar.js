(function(){

angular.module('indigoeln').directive('toolbar', toolbar);

toolbar.$inject = ['$mdDialog', '$rootScope'];

angular.module('indigoeln').controller('NewExperimentController', NewExperimentController);

NewExperimentController.$inject = ['$scope', '$mdDialog'];

function toolbar($mdDialog, $rootScope) {
    return {
    	scope : {},
        restrict : 'E',
        templateUrl : 'views/toolbar/toolbar.html',
        link : function(scope, elem, attrs) {
            scope.newExperiment = newExperiment;
            
            function newExperiment() {
                $mdDialog.show({
                  controller: NewExperimentController,
                  templateUrl: 'views/toolbar/newexperiment.html',
                  parent: angular.element(document.body),
                  clickOutsideToClose:false
                })
                .then(function(experiment) {
                	// call service to create experiment on server
                	$rootScope.$broadcast('new-experiment', {experiment : experiment});
                }, function() {
                });
            }
        }
    }
}

function NewExperimentController($scope, $mdDialog) {
  $scope.newexperiment = {};
	
  $scope.hide = function() {
    $mdDialog.hide();
  };
  $scope.cancel = function() {
    $mdDialog.cancel();
  };
  $scope.create = function() {
    $mdDialog.hide($scope.newexperiment);
  };
}

})();