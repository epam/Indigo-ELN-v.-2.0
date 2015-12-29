(function(){
    'use strict';

    angular.module('indigoeln').controller('NewProjectCtrl', NewProjectCtrl);
    NewProjectCtrl.$inject = ['projectService', '$scope', '$log', '$uibModalInstance'];

    function NewProjectCtrl(projectService, $scope, $log, $uibModalInstance) {
        $scope.project = {};
        $scope.sourceUsers = [];
        $scope.destUsers = [];
        $scope.selectedUser = null;

        $scope.ok = function() {
            $scope.project.users = $scope.destUsers;
            projectService.createNewProject($scope.project)
                .success(function(response) {
                    $log.log("Project created successfully.");
                })
                .error(function(response) {
                    $log.warn("Project wasn't created.");
                });
            $uibModalInstance.close($scope.project);
        };

        $scope.cancel = function() {
            $uibModalInstance.dismiss();
        };

        $scope.allToDest = function() {
            $scope.destUsers = $scope.destUsers.concat($scope.sourceUsers);
            $scope.destUsers.sort();
            $scope.sourceUsers = [];
        }

        $scope.toDest = function() {
            var itemId = $scope.sourceUsers.indexOf($scope.selectedUser);

            if (itemId != -1) {
                $scope.destUsers.push($scope.selectedUser);
                $scope.destUsers.sort();
                $scope.sourceUsers.splice(itemId, 1);
            }
        };

        $scope.allToSource = function() {
            $scope.sourceUsers = $scope.sourceUsers.concat($scope.destUsers);
            $scope.sourceUsers.sort();
            $scope.destUsers = [];
        }

        $scope.toSource = function() {
            var itemId = $scope.destUsers.indexOf($scope.selectedUser);

            if (itemId != -1) {
                $scope.sourceUsers.push($scope.selectedUser);
                $scope.sourceUsers.sort();
                $scope.destUsers.splice(itemId, 1);
            }
        };

        $scope.selectUser = function(user) {
            $scope.selectedUser = user;
        }

        var init = function() {
            projectService.getUsers()
                .success(function(response) {
                    $scope.sourceUsers = response;
                })
                .error(function(response) {
                });
        };

        init();
    }

})();