'use strict';

angular.module('indigoeln')
    .controller('UserManagementController', function ($scope, User, ParseLinks, $filter, roles) {
        $scope.users = [];
        $scope.roles = roles;

        $scope.page = 1;
        $scope.loadAll = function () {
            User.query({page: $scope.page - 1, size: 20}, function (result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.totalItems = headers('X-Total-Count');
                $scope.users = result;
            });
        };

        $scope.loadPage = function (page) {
            $scope.page = page;
            $scope.loadAll();
        };
        $scope.loadAll();

        $scope.setActive = function (user, isActivated) {
            user.activated = isActivated;
            User.update(user, function () {
                $scope.loadAll();
                $scope.clear();
            });
        };

        $scope.clear = function () {
            $scope.user = null;
            $scope.editForm.$setPristine();
            $scope.editForm.$setUntouched();
        };

        var onSaveSuccess = function (result) {
            $scope.isSaving = false;
            $scope.user = null;
            $scope.loadAll();
        };

        var onSaveError = function (result) {
            $scope.isSaving = false;
            $scope.loadAll();
        };

        $scope.save = function () {
            $scope.isSaving = true;
            if ($scope.user.id != null) {
                User.update($scope.user, onSaveSuccess, onSaveError);
            } else {
                User.save($scope.user, onSaveSuccess, onSaveError);
            }
        };

        $scope.create = function () {
            $scope.user = {
                id: null, login: null, firstName: null, lastName: null, email: null,
                activated: true, roles: null
            };
        };

        $scope.edit = function (user) {
            $scope.loadAll();
            $scope.user = _.extend({}, user);
        };

        $scope.search = function () {
            User.query({page: $scope.page - 1, size: 20}, function (result, headers) {
                $scope.links = ParseLinks.parse(headers('link'));
                $scope.totalItems = headers('X-Total-Count');
                $scope.users = $filter('filter')(result, $scope.searchText);
            });
        };
    });