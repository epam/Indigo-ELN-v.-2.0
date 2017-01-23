angular.module('indigoeln')
    .constant('PermissionViewManagementConfig', {
        url: '/permissions-view',
        onEnter: ['$rootScope', '$stateParams', '$state', '$uibModal', 'PermissionManagement',
            function ($rootScope, $stateParams, $state, $uibModal) {
                var that = this;
                $uibModal.open({
                    templateUrl: 'scripts/components/permissions/permissions-view/permission-view.html',
                    controller: 'PermissionViewManagementController',
                    size: 'lg'
                }).result.then(function (result) {
                    $state.go('^');
                }, function () {
                    $state.go('^');
                });
            }]
        });