var run = require('./simple-input.run');
var simpleInput = require('./simple-input.directive');

module.export = angular
    .module('indigoeln.simpleInput', [])

    .run(run)

    .directive('simpleInput', simpleInput)

    .name;
