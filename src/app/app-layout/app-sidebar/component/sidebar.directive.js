var template = require('./sidebar.html');
var roles = require('../../../permissions/permission-roles.json');

function sidebar() {
    return {
        scope: true,
        template: template,
        controller: SidebarController,
        bindToController: true,
        controllerAs: 'vm'
    };
}

SidebarController.$inject = ['$scope', '$state', '$stateParams', 'sidebarCache', 'entityTreeService'];

function SidebarController($scope, $state, $stateParams, sidebarCache, entityTreeService) {
    var vm = this;

    vm.CONTENT_EDITOR = roles.CONTENT_EDITOR;
    vm.USER_EDITOR = roles.USER_EDITOR;
    vm.ROLE_EDITOR = roles.ROLE_EDITOR;
    vm.TEMPLATE_EDITOR = roles.TEMPLATE_EDITOR;
    vm.DICTIONARY_EDITOR = roles.DICTIONARY_EDITOR;
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

module.exports = sidebar;
