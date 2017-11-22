var template = require('./permission-view.html');

var permissionViewConfig = {
    url: '/permissions-view',
    onEnter: ['$rootScope', '$stateParams', '$state', '$uibModal', 'permissionService',
        function($rootScope, $stateParams, $state, $uibModal) {
            $uibModal.open({
                template: template,
                controller: 'PermissionViewController',
                controllerAs: 'vm',
                size: 'lg'
            })
                .result
                .finally(function() {
                    $state.go('^');
                });
        }]
};

module.exports = permissionViewConfig;
