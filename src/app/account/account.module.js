var accountConfig = require('./account.config');
var LoginController = require('./login/login.controller');

module.exports = angular
    .module('indigoeln.account', [])

    .controller('LoginController', LoginController)

    .config(accountConfig)

    .name;
