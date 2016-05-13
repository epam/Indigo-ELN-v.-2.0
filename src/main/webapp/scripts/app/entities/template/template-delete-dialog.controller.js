angular.module('indigoeln')
    .controller('TemplateDeleteController', function ($scope, $uibModalInstance, $stateParams, Template) {
        $scope.clear = function () {
            $uibModalInstance.dismiss('cancel');
        };
        $scope.confirmDelete = function () {
            Template.delete({id: $stateParams.id},
                function () {
                    $uibModalInstance.close(true);
                });
        };

    });
