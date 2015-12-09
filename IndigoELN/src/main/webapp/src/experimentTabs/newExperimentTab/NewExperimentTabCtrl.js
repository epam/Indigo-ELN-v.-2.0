(function(){

angular.module('indigoeln').controller('NewExperimentTabCtrl', NewExperimentTabCtrl);

NewExperimentTabCtrl.$inject = ['$scope', '$rootScope'];

function NewExperimentTabCtrl($scope, $rootScope) {
  $scope.experiment = {};
  
  $scope.templates = [{ name : 'Template 1'}, { name : 'Template 2'}, { name : 'Template 3'}, { name : 'Template 4'}];
    
  $scope.ok = function() {
    $rootScope.$broadcast('created-experiment', {experiment : $scope.experiment});
    $scope.removeTab($scope.tab);
  };
  
  $scope.cancel = function() {
    $scope.removeTab($scope.tab);
  };
  
  $scope.select = function(template) {
    $scope.selected = template;
  }
}

})();