angular.module('indigoeln')
    .controller('TabManagerController', function ($scope, $stateParams, TabManager) {
        $scope.tabs = TabManager.getTabs();
        $scope.activeTab = TabManager.getActiveTab();

        var onTabsChanged = $scope.$on('tabs-changed', function() {
            $scope.tabs = TabManager.getTabs();
            $scope.activeTab = TabManager.getActiveTab();
        });
        $scope.$on('$destroy', function() {
            onTabsChanged();
        });

        $scope.onTabClick = function (tabName) {
            TabManager.goToTab(tabName);
        };

        $scope.onCloseTabClick = function (tabName) {
            TabManager.closeTab(tabName);
        };
    });
