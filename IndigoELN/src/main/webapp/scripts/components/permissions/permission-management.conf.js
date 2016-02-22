'use strict';

angular.module('indigoeln')
    .constant('PermissionManagementConfig', {
        url: '/permissions',
        onEnter: function ($rootScope, $stateParams, $state, $uibModal, PermissionManagement) {
            var that = this;
            $uibModal.open({
                templateUrl: 'scripts/components/permissions/permission-management.html',
                controller: 'PermissionManagementController',
                size: 'lg',
                resolve: {
                    users: function (User) {
                        return User.query().$promise;
                    },
                    permissions: function () {
                        return that.permissions;
                    }
                }
            }).result.then(function (result) {
                PermissionManagement.setAccessList(result);
                $rootScope.$broadcast('access-list-changed');
                $state.go('^');
            }, function () {
                $state.go('^');
            });
        }
    });


//
//
//return {
//    get: function(entity) {
//        if (entity.accessList) {
//            return entity.accessList;
//        } else return [];
//    },
//    set: function(list, entity) {
//        if (entity.accessList) {
//            entity.accessList = list;
//        }
//    }
//};