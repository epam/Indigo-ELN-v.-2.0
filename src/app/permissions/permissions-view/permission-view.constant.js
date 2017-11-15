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
            }).result.then(function() {
                $state.go('^');
            }, function() {
                $state.go('^');
            });
        }]
};

module.exports = permissionViewConfig;
