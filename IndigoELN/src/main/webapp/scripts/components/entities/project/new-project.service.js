'use strict';

angular.module('indigoeln').factory('projectService', projectService);
projectService.$inject = ['$http'];

function projectService($http) {
    var API = {};

    API.getUsers = function () {
        return $http({
            method: 'GET',
            url: 'api/account/all'
        });
    }

    API.getProjectByName = function (projectName) {
        return $http({
            method: 'POST',
            url: 'api/projects/getProjectByName', //todo: wrong for rest style api
            data: $.param({projectName: projectName}),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
    }

    API.createNewProject = function (newProjectData) {
        return $http({
            method: 'POST',
            url: 'api/projects/createNewProject', //todo: wrong for rest style api
            data: angular.toJson(newProjectData),
            headers: {'Content-Type': 'application/json'}
        });
    }

    return API;
}