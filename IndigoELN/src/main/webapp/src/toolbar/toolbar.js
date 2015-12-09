(function(){

angular.module('indigoeln').directive('toolbar', toolbar);

toolbar.$inject = ['$uibModal', '$rootScope'];

function toolbar($uibModal, $rootScope) {
    return {
    	scope : {},
        restrict : 'E',
        templateUrl : 'src/toolbar/toolbar.html',
        link : function(scope, elem, attrs) {
            scope.newExperiment = newExperiment;
            scope.newNotebook = newNotebook;
            
            function newExperiment(event) {
                $rootScope.$broadcast('new-experiment', {});
                event.preventDefault();
            }
            
            function newNotebook(event) {
                var modalInstance = $uibModal.open({
                  animation: true,
                  templateUrl: 'src/newnotebook/newnotebook.html',
                  controller: 'NewNotebookCtrl'
                });
                
                modalInstance.result.then(function (notebook) {
                    $rootScope.$broadcast('created-notebook', {notebook : notebook});
                }, function () {
                });                
            }
        }
    }
}
})();