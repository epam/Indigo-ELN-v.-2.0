angular.module('indigoeln')
    .constant('PermissionViewManagementConfig', {
        url: '/permissions-view',
        onEnter: ['$rootScope', '$stateParams', '$state', '$uibModal', 'PermissionManagement',
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