'use strict';

angular.module('indigoeln')
    .controller('StructureExportModalController', function ($scope, $uibModalInstance, $window, structureToSave, structureType) {

        $scope.structureToSave = structureToSave;
        $scope.format = "molfile"; // molfile format by deafult

        $scope.download = function() {

            var NUM_MAX = 999,
                NUM_MIN = 100,
                ORDER = 1000;

            var text = $scope.structureToSave.replace(/\n/g, '\r\n'),
                blob = new Blob([text], {type: 'text/plain'}),
                url = $window.URL || $window.webkitURL,
                isMol = structureType === 'molecule',
                fileExt = isMol ? 'mol' : 'rxn';

            $scope.filename = fileExt + '-' + Math.floor(Math.random(NUM_MIN,NUM_MAX)*ORDER) + '.' + fileExt;
            $scope.urlObj = url.createObjectURL(blob);

            $uibModalInstance.close();
        };

        $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

    });