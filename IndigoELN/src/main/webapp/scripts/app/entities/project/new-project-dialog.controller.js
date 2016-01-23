'use strict';

angular.module('indigoeln').controller('NewProjectDialogController', function (projectService, $scope, $log, $uibModalInstance, users) {
    $scope.project = {};
    $scope.sourceUsers = users;
    $scope.destUsers = [];
    $scope.selectedUser = null;

    $scope.ok = function () {
        $scope.project.accessList = [];
        $scope.destUsers.forEach(function (element) {
            var userPermission = {
                userId: element.id, // TODO must be userId
                // TODO must be configurable in UI
                permissions: 'RERSCSUE' // permissions for OWNER (Read Entity, Read Sub-Entity, Create Sub-Entity, Update Entity)
            };
            $scope.project.accessList.push(userPermission);
        });
        projectService.save($scope.project, function (response) {
            $log.log('Project created successfully.');
            $uibModalInstance.close($scope.project);
        }, function (response) {
            $log.warn('Project wasn\'t created.');
        });
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

