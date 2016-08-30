angular.module('indigoeln')
    .directive('preferredCompoundDetails', function (InfoEditor, AppValues, CalculationService) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/preferCompoundDetails/preferCompoundDetails.html',
            controller: function ($scope) {
                $scope.model = $scope.model || {};
                $scope.model.preferCompoundDetails = {};
                $scope.showStructure = false;


                var onCompoundSummaryRowSelectedEvent = $scope.$on('batch-summary-row-selected', function (event, data) {
                    $scope.model.preferCompoundDetails = data.row;
                    if(data.row.structure){
                        $scope.structureImage = data.row.structure.image;
                    }else{
                        $scope.structureImage = '';
                    }
                });
               var onCompoundStructureChanged = $scope.$on('product-batch-structure-changed', function (event, data) {
                    if(data.structure){
                        $scope.structureImage = data.structure.image;
                    }else{
                        $scope.structureImage = '';
                    }
                });

                var onCompoundSummaryRowDeselectedEvent = $scope.$on('batch-summary-row-deselected', function () {
                    $scope.model.preferCompoundDetails = {};
                    $scope.structureImage = '';
                });



                $scope.$on('$destroy', function () {
                    onCompoundSummaryRowSelectedEvent();
                    onCompoundSummaryRowDeselectedEvent();
                    onCompoundStructureChanged();
                });

            }
        };
    });