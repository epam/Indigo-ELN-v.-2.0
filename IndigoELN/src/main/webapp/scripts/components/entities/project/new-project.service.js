(function () {
    'use strict';

    angular.module('indigoeln').factory('projectService', projectService);
    projectService.$inject = ['$http'];

    function projectService($http) {
        var API = {};

        API.getUsers = function () {
            return $http({
                method : 'GET',
                url : '/indigoeln/service/getUsers'
            });
        }

        API.getProjectByName = function (projectName) {
            return $http({
                method : 'POST',
                url : '/indigoeln/service/getProjectByName',
                data: $.param({projectName: projectName}),
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            });
        }

        API.createNewProject = function (newProjectData) {
            return $http({
                method : 'POST',
                url : '/indigoeln/service/createNewProject',
                data: angular.toJson(newProjectData),
                headers: {'Content-Type': 'application/json'}
            });
        }

        return API;
    }
})();