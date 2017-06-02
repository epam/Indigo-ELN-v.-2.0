angular.module('indigoeln')
    .directive('entitiesControls', function(EntitiesBrowser, $state) {
        return {
            templateUrl: 'scripts/app/entities/entities-controls.html',
            link: function($scope, el, attr) {
            	$scope.isDashboard = attr.isDashboard;
            },
            controller: function($scope) {
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
                })
                $scope.onTabClick = function(tab) {
                    EntitiesBrowser.goToTab(tab);
                }
                $scope.openSearch = function() {
                    $state.go('entities.search-panel')
                };

            }
        };
    })
