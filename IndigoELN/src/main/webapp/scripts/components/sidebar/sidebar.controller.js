(function() {
    angular
        .module('indigoeln')
        .controller('SidebarController', SidebarController);

    function SidebarController($scope, $state, $stateParams, sidebarCache, entityTreeService) {
        var vm = this;

        vm.CONTENT_EDITOR = 'CONTENT_EDITOR';
        vm.USER_EDITOR = 'USER_EDITOR';
        vm.ROLE_EDITOR = 'ROLE_EDITOR';
        vm.TEMPLATE_EDITOR = 'TEMPLATE_EDITOR';
        vm.DICTIONARY_EDITOR = 'DICTIONARY_EDITOR';
        vm.POPOVER_TEMPLATE = 'scripts/components/sidebar/sidebar-popover-template.html';
        vm.ADMINISTRATION_AUTHORITIES = [vm.USER_EDITOR, vm.ROLE_EDITOR, vm.TEMPLATE_EDITOR, vm.DICTIONARY_EDITOR].join(',');
        vm.$state = $state;

        vm.getTreeItemById = getTreeItemById;
        vm.toggleAdministration = toggleAdministration;
        vm.toggleProjects = toggleProjects;
        vm.toggleMyProjects = toggleMyProjects;
        vm.onSelectNode = onSelectNode;

        init();

        function init() {
            vm.allProjectIsCollapsed = sidebarCache.get('allProjectIsCollapsed');
            vm.bookmarksIsCollapsed = sidebarCache.get('bookmarksIsCollapsed');
            vm.adminToggled = sidebarCache.get('adminToggled');
            vm.selectedFullId = entityTreeService.getFullIdFromParams($stateParams);

            bindEvents();
        }

        function onSelectNode(fullId) {
            if (vm.selectedFullId !== fullId) {
                vm.selectedFullId = fullId;
            }
        }

        function getTreeItemById(items, itemId) {
            return _.find(items, function(projectItem) {
                return projectItem.id === itemId;
            });
        }

        function toggleAdministration() {
            vm.adminToggled = !vm.adminToggled;
            sidebarCache.put('adminToggled', vm.adminToggled);
        }

        function toggleProjects() {
            vm.allProjectIsCollapsed = !vm.allProjectIsCollapsed;
            sidebarCache.put('allProjectIsCollapsed', vm.allProjectIsCollapsed);
        }

        function toggleMyProjects() {
            vm.bookmarksIsCollapsed = !vm.bookmarksIsCollapsed;
            sidebarCache.put('bookmarksIsCollapsed', vm.bookmarksIsCollapsed);
        }

        function bindEvents() {
            $scope.$on('$stateChangeSuccess', function(event, toState, toParams) {
                vm.onSelectNode(entityTreeService.getFullIdFromParams(toParams));
            });

            $scope.$on('experiment-status-changed', function(event, data) {
                _.forEach(data, function(status, fullId) {
                    entityTreeService.updateStatus(fullId, status);
                });
            });
        }
    }
})();
