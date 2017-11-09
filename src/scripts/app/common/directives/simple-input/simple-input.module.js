require('./simple-input.less');
var simpleInput = require('./simple-input.directive');
var run = require('./simple-input.run');

module.exports = angular
    .module('indigoeln.simpleInput', [])

    .directive('simpleInput', simpleInput)

    .run(run)

    .name;
