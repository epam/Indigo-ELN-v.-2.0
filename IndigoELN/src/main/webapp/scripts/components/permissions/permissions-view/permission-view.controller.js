angular.module('indigoeln')
    .controller('PermissionViewManagementController',
        function ($scope, $uibModalInstance, PermissionManagement) {

            $scope.accessList = PermissionManagement.getAccessList();
            $scope.entity = PermissionManagement.getEntity();


            $scope.close = function() {
                $uibModalInstance.dismiss('cancel');
            };

    });
