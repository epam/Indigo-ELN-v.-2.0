angular.module('indigoeln')
    .constant('permissionViewManagementConfig', {
        url: '/permissions-view',
        onEnter: ['$rootScope', '$stateParams', '$state', '$uibModal', 'permissionManagementService',
            function($rootScope, $stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'scripts/components/permissions/permissions-view/permission-view.html',
                    controller: 'PermissionViewManagementController',
                    controllerAs: 'vm',
                    size: 'lg'
                }).result.then(function() {
                    $state.go('^');
                }, function() {
                    $state.go('^');
                });
            }]
    });
