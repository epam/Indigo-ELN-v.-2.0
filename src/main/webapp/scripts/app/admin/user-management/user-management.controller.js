angular.module('indigoeln')
    .controller('UserManagementController', function ($scope, $uibModal, User, ParseLinks, $filter, pageInfo) {
        $scope.users = [];
        $scope.roles = pageInfo.roles;

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
        };

        var onSaveSuccess = function () {
            $scope.isSaving = false;
            $scope.user = null;
            $scope.loadAll();
        };

        var onSaveError = function () {
            $scope.isSaving = false;
            $scope.loadAll();
        };

        $scope.save = function () {
            $scope.isSaving = true;
            if ($scope.user.group) {
                $scope.user.group = $scope.user.group.name;
            }
            if ($scope.user.id) {
                User.update($scope.user, onSaveSuccess, onSaveError);
            } else {
                User.save($scope.user, onSaveSuccess, onSaveError);
            }
        };

        $scope.create = function () {
            $scope.user = {
                id: null, login: null, firstName: null, lastName: null, email: null,
                activated: true, roles: null, group: null
            };
        };

        $scope.edit = function (user) {
            if (user.group) {
                user.group = { name: user.group} ;
            }
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

        $scope.changePassword = function () {
            $uibModal.open({
                animation: true,
                size: 'sm',
                template: '<div class="modal-body">' +
                '<form id="editPassword" name="editPassword" role="form" novalidate class="form-horizontal" autocomplete="off">' +
                '<input type="password" name="password" id="password_fake" class="hidden" autocomplete="off">' +
                '<input style="display:none" type="password" name="fakepasswordremembered"/>' +
                '<div class="container" ><div class="row"><div class="col-xs-3">' +
                '<my-input style="width:250px;" my-label="New Password" my-label-vertical="true" ' +
                'my-name="password" my-type="password" my-model="password" ' +
                'my-validation-required="password == null" my-validation-maxlength="50">' +
                '</my-input></div></div></div></div>' +
                '<div class="modal-footer text-right">' +
                '<button class="btn btn-primary" type="button" ng-click="ok()" ng-disabled="editPassword.$invalid">Ok</button>' +
                '<button class="btn btn-default" type="button" ng-click="cancel()">Cancel</button>' +
                '</div>',
                controller: function ($scope, $uibModalInstance) {
                    $scope.cancel = function () {
                        $uibModalInstance.dismiss('cancel');
                    };
                    $scope.ok = function () {
                        $uibModalInstance.close($scope.password);
                    };
                }
            }).result.then(function (password) {
                $scope.user.password = password;
            });
        };
    });