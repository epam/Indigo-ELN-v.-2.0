var template = require('./sidebar.html');

function sidebar() {
    return {
        scope: true,
        template: template,
        controller: SidebarController,
        bindToController: true,
        controllerAs: 'vm'
    };
}

SidebarController.$inject = ['$scope', '$state', '$stateParams', 'sidebarCacheService', 'entityTreeService'];

function SidebarController($scope, $state, $stateParams, sidebarCacheService, entityTreeService) {
    var vm = this;

    vm.CONTENT_EDITOR = 'CONTENT_EDITOR';
    vm.USER_EDITOR = 'USER_EDITOR';
    vm.ROLE_EDITOR = 'ROLE_EDITOR';
    vm.TEMPLATE_EDITOR = 'TEMPLATE_EDITOR';
    vm.DICTIONARY_EDITOR = 'DICTIONARY_EDITOR';
    vm.ADMINISTRATION_AUTHORITIES = [vm.USER_EDITOR, vm.ROLE_EDITOR, vm.TEMPLATE_EDITOR, vm.DICTIONARY_EDITOR]
        .join(',');
    vm.$state = $state;

    vm.getTreeItemById = getTreeItemById;
    vm.toggleAdministration = toggleAdministration;
    vm.toggleProjects = toggleProjects;
    vm.toggleMyProjects = toggleMyProjects;
    vm.onSelectNode = onSelectNode;

    init();

    function init() {
        vm.allProjectIsCollapsed = sidebarCacheService.get('allProjectIsCollapsed');
        vm.bookmarksIsCollapsed = sidebarCacheService.get('bookmarksIsCollapsed');
        vm.adminToggled = sidebarCacheService.get('adminToggled');
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
        sidebarCacheService.put('adminToggled', vm.adminToggled);
    }

    function toggleProjects() {
        vm.allProjectIsCollapsed = !vm.allProjectIsCollapsed;
        sidebarCacheService.put('allProjectIsCollapsed', vm.allProjectIsCollapsed);
    }

    function toggleMyProjects() {
        vm.bookmarksIsCollapsed = !vm.bookmarksIsCollapsed;
        sidebarCacheService.put('bookmarksIsCollapsed', vm.bookmarksIsCollapsed);
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

module.exports = sidebar;
