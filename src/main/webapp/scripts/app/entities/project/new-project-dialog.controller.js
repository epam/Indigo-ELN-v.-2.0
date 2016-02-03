'use strict';

angular.module('indigoeln').controller('NewProjectDialogController', function ($scope, $rootScope, $state, $log, $uibModalInstance, Project, users) {
    $scope.project = {};
    $scope.sourceUsers = users;
    $scope.destUsers = [];
    $scope.selectedUsers = [];
    $scope.canSort = false;
    $scope.filterData = '';

    var onCreateSuccess = function (result) {
        $log.log('Project created successfully.');
        $uibModalInstance.close(result);
        $scope.isSaving = false;
        $state.go('project', {id: result.id});
        $rootScope.$broadcast('project-created', {id: result.id});
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
        for (var selectedUserIndex in $scope.selectedUsers) {
            var itemId = $scope.sourceUsers.indexOf($scope.selectedUsers[selectedUserIndex]);

            if (itemId !== -1) {
                $scope.destUsers.push($scope.selectedUsers[selectedUserIndex]);
                $scope.destUsers.sort();
                $scope.sourceUsers.splice(itemId, 1);
            }
        }
    };

    $scope.allToSource = function () {
        $scope.sourceUsers = $scope.sourceUsers.concat($scope.destUsers);
        $scope.sourceUsers.sort();
        $scope.destUsers = [];
    };

    $scope.toSource = function () {
        for (var selectedUserIndex in $scope.selectedUsers) {
            var itemId = $scope.destUsers.indexOf($scope.selectedUsers[selectedUserIndex]);

            if (itemId !== -1) {
                $scope.sourceUsers.push($scope.selectedUsers[selectedUserIndex]);
                $scope.sourceUsers.sort();
                $scope.destUsers.splice(itemId, 1);
            }
        }
    };

    $scope.selectUser = function (event, user) {
        if (event.ctrlKey) {
            $scope.selectedUsers.push(user);
        } else {
            $scope.selectedUsers = [user];
        }

    };

    $scope.keyPressed = function(event) {
        console.log(event);
        // 'is letter' check
        if ((event.charCode >= 65 && event.charCode <= 90) || (event.charCode >= 97 && event.charCode <= 122)) {
            $scope.filterData = String.fromCharCode(event.charCode);
        } else if (event.charCode === 32) {
            $scope.filterData = '';
        }
    };

    $scope.userFilter = function(user) {
        if (user.login.indexOf($scope.filterData) > -1) {
            return true;
        }
        return false;
    };

});

