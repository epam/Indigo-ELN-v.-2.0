(function () {
    angular
        .module('indigoeln')
        .directive('indigoEntitiesControls', indigoEntitiesControls);

    function indigoEntitiesControls() {
        return {
            restrict: 'E',
            templateUrl: 'scripts/app/entities/entities-controls.html',
            link: link,
            controller: controller
        };
    }

    /* @ngInject */
    function link($scope, $attr) {
        $scope.isDashboard = $attr.isDashboard;
    }

    /* @ngInject */
    function controller($scope, $state, EntitiesBrowser) {
        $scope.CONTENT_EDITOR = 'CONTENT_EDITOR';
        $scope.PROJECT_CREATOR = 'PROJECT_CREATOR';
        $scope.NOTEBOOK_CREATOR = 'NOTEBOOK_CREATOR';
        $scope.EXPERIMENT_CREATOR = 'EXPERIMENT_CREATOR';
        $scope.GLOBAL_SEARCH = 'GLOBAL_SEARCH';
        $scope.PROJECT_CREATORS = [$scope.CONTENT_EDITOR, $scope.PROJECT_CREATOR].join(',');
        $scope.NOTEBOOK_CREATORS = [$scope.CONTENT_EDITOR, $scope.NOTEBOOK_CREATOR].join(',');
        $scope.EXPERIMENT_CREATORS = [$scope.CONTENT_EDITOR, $scope.EXPERIMENT_CREATOR].join(',');
        $scope.ENTITY_CREATORS = [$scope.CONTENT_EDITOR, $scope.PROJECT_CREATOR, $scope.NOTEBOOK_CREATOR, $scope.EXPERIMENT_CREATOR].join(',');

        EntitiesBrowser.getTabs(function(tabs) {
            $scope.entities = tabs;
        });

        $scope.onTabClick = function(tab) {
            EntitiesBrowser.goToTab(tab);
        };

        $scope.openSearch = function() {
            $state.go('entities.search-panel');
        };

        $scope.canSave = function() {
            return !!EntitiesBrowser.saveCurrentEntity && !!EntitiesBrowser.getCurrentForm() && EntitiesBrowser.getCurrentForm().$dirty;
        };

        $scope.save = function() {
            EntitiesBrowser.saveCurrentEntity();
        };

        $scope.canPrint = function() {
            var actions = EntitiesBrowser.getEntityActions();
            return actions && actions.print;
        };

        $scope.print = function() {
            EntitiesBrowser.getEntityActions().print();
        };

        $scope.canDuplicate = function() {
            var actions = EntitiesBrowser.getEntityActions();
            return actions && actions.duplicate;
        };

        $scope.duplicate = function() {
            EntitiesBrowser.getEntityActions().duplicate();
        };
    }
})();