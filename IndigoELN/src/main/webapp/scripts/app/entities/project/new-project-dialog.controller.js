'use strict';

angular.module('indigoeln').controller('NewProjectDialogController', function ($scope, $state, $log, $uibModalInstance, Project, users) {
    $scope.project = {};
    $scope.sourceUsers = users;
    $scope.destUsers = [];
    $scope.selectedUser = null;

        var onCreateSuccess = function (result) {
            $log.log('Project created successfully.');
            $uibModalInstance.close(result);
            $scope.isSaving = false;
            $state.go('project', {id: result.id});
        };

        var onCreateError = function (result) {
            $scope.isSaving = false;
            $log.warn('Project wasn\'t created.');
        };

        $scope.create = function () {
            $scope.project.accessList = [];
            $scope.destUsers.forEach(function (element) {
                var userPermission = {
                    userId: element.id, // TODO must be userId
                    // TODO must be configurable in UI
                    permissions: ['READ_ENTITY', 'READ_SUB_ENTITY', 'CREATE_SUB_ENTITY'] // permissions for USER (Read Entity, Read Sub-Entity, Create Sub-Entity)
                };
                $scope.project.accessList.push(userPermission);
            });
            $scope.isSaving = true;
            //$scope.project.author = currentUser;
            $scope.project.users = $scope.destUsers;
            if ($scope.project.id) {
                Project.update($scope.project, onCreateSuccess, onCreateError);
            } else {
                Project.save($scope.project, onCreateSuccess, onCreateError);
            }
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss();
        };

        $scope.allToDest = function () {
            $scope.destUsers = $scope.destUsers.concat($scope.sourceUsers);
            $scope.destUsers.sort();
            $scope.sourceUsers = [];
        };

        $scope.toDest = function () {
            var itemId = $scope.sourceUsers.indexOf($scope.selectedUser);

            if (itemId !== -1) {
                $scope.destUsers.push($scope.selectedUser);
                $scope.destUsers.sort();
                $scope.sourceUsers.splice(itemId, 1);
            }
        };

        $scope.allToSource = function () {
            $scope.sourceUsers = $scope.sourceUsers.concat($scope.destUsers);
            $scope.sourceUsers.sort();
            $scope.destUsers = [];
        };

        $scope.toSource = function () {
            var itemId = $scope.destUsers.indexOf($scope.selectedUser);

            if (itemId !== -1) {
                $scope.sourceUsers.push($scope.selectedUser);
                $scope.sourceUsers.sort();
                $scope.destUsers.splice(itemId, 1);
            }
        };

        $scope.selectUser = function (user) {
            $scope.selectedUser = user;
        };

});

