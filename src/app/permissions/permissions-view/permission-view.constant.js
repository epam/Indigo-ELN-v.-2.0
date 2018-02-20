var permissionViewConfig = {
    url: '/permissions-view',
    onEnter: ['permissionModal', '$state',
        function(permissionModal, $state) {
            permissionModal.showPopup()
                .finally(function() {
                    $state.go('^');
                });
        }],
    onExit: ['permissionModal',
        function(permissionModal) {
            permissionModal.close();
        }]
};

module.exports = permissionViewConfig;
