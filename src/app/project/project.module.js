var projectConfig = require('./project.config');
var ProjectController = require('./component/project.controller');

var permissions = require('../permissions/permissions.module');

var dependencies = [
    permissions
];

module.exports = angular
    .module('indigoeln.project', dependencies)

    .controller('ProjectController', ProjectController)

    .config(projectConfig)

    .name;
