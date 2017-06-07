(function () {
    angular
        .module('indigoeln')
        .directive('indigoPreferredCompoundDetails', indigoPreferredCompoundDetails);

    function indigoPreferredCompoundDetails() {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'scripts/components/entities/template/components/preferCompoundDetails/preferCompoundDetails.html',
            controller: controller
        };
    }

    /* @ngInject */
    function controller($scope, EntitiesBrowser) {
        $scope.model = $scope.model || {};
        $scope.model.preferCompoundDetails = $scope.model.preferCompoundDetails || {};
        $scope.model.preferredCompoundSummary = $scope.model.preferredCompoundSummary || {};
        $scope.showStructure = false;
        $scope.showSummary = false;

        $scope.notebookId = EntitiesBrowser.activeTab.$$title;

        $scope.selectControl = {};

        $scope.onSelectCompound = function () {
            if ($scope.share.selectedRow) {
                $scope.share.selectedRow.$$selected = false;
            }
            $scope.share.selectedRow = $scope.selectedCompound || {};
            $scope.share.selectedRow.$$selected = true;
            selectCompound($scope.share.selectedRow);
        };

        function selectCompound(row) {
            $scope.model.preferCompoundDetails = row;


            if (row.structure) {
                $scope.structureImage = row.structure.image;
            } else {
                $scope.structureImage = '';
            }

            $scope.selectedCompound = row;
            $scope.selectControl.setSelection(row);
        }

        function deselectCompound() {
            $scope.model.preferCompoundDetails = {};
            $scope.structureImage = '';
            $scope.selectControl.unSelect();
        }

        var onCompoundSummaryRowSelectedEvent = $scope.$on('batch-summary-row-selected', function (event, data) {
            selectCompound(data.row);
        });

        var onCompoundStructureChanged = $scope.$on('product-batch-structure-changed', function (event, data) {
            if (data.structure) {
                $scope.structureImage = data.structure.image;
            } else {
                $scope.structureImage = '';
            }
        });

        var onCompoundSummaryRowDeselectedEvent = $scope.$on('batch-summary-row-deselected', function () {
            deselectCompound();
        });


        $scope.$on('$destroy', function () {
            onCompoundSummaryRowSelectedEvent();
            onCompoundSummaryRowDeselectedEvent();
            onCompoundStructureChanged();
        });

    }
})();