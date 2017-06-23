(function () {
    angular
        .module('indigoeln')
        .controller('ExperimentPreviewController', ExperimentPreviewController);

    /* @ngInject */
    function ExperimentPreviewController($state, $rootScope, $uibModal, pageInfo,
                                         experimentPdfCreator, SignatureTemplates, SignatureDocument, EntitiesCache) {
        var vm = this;

        vm.fullPrint = $state.current.data.isFullPrint;
        vm.experiment = pageInfo.experiment;
        vm.notebook = pageInfo.notebook;
        vm.project = pageInfo.project;

        vm.batchDetails = vm.experiment.components.productBatchDetails;
        vm.batchSummary = vm.experiment.components.productBatchSummary;
        vm.conceptDetails = vm.experiment.components.conceptDetails;
        vm.reactionDetails = vm.experiment.components.reactionDetails;
        vm.reaction = vm.experiment.components.reaction;
        vm.molecule = vm.experiment.components.molecule;
        vm.experimentDescription = vm.experiment.components.experimentDescription;
        vm.stoichTable = vm.experiment.components.stoichTable;
        vm.preferredCompounds = vm.experiment.components.preferredCompoundSummary;
        vm.currentDate = Date.now();

        vm.print = print;
        vm.toDigits = toDigits;
        vm.joinArr = joinArr;
        vm.submit = submit;

        if (vm.batchSummary) {
            vm.registrationSummary = {
                batches: vm.batchSummary.batches.filter(function (b) {
                    return b.conversationalBatchNumber;
                })
            };
        }

        function print() {
            vm.isPrinting = true;
            var fileName = vm.notebook.name + '-' + vm.experiment.name;
            if (vm.experiment.experimentVersion > 1 || !vm.experiment.lastVersion) {
                fileName += ' v' + vm.experiment.experimentVersion;
            }
            experimentPdfCreator.createPDF(fileName, downloadReport);
        }

        function toDigits(o) {
            if (!o || o.value === 0) {
                return '';
            }

            var val = o.value ? Number(o.value) : Number(o);
            if (!val) {
                return '';
            }
            var unit = o.unit ? ' ' + o.unit : '';
            return val.toFixed(3).replace(/0+$/, '').replace(/\.+$/, '') + unit;
        }

        function joinArr(users, fi) {
            return users ? users.map(function (u) {
                    return fi ? u[fi] : u.name;
                }).join(', ') : null;
        }

        function submit() {
            var fileName = vm.notebook.name + '-' + vm.experiment.name;
            if (vm.experiment.experimentVersion > 1 || !vm.experiment.lastVersion) {
                fileName += ' v' + vm.experiment.experimentVersion;
            }
            experimentPdfCreator.createPDF(fileName, selectTemplate);
        }

        function downloadReport(response) {
            var hiddenFrameId = 'hiddenDownloader';
            var hiddenDownloader = $('#' + hiddenFrameId);
            if (!hiddenDownloader.length) {
                hiddenDownloader = $('<iframe id="' + hiddenFrameId + '" style="display:none"/>');
                $('body').append(hiddenDownloader);
            }
            hiddenDownloader.attr('src', 'api/print?fileName=' + response.fileName);
            vm.isPrinting = false;
        }

        function onCompleteSuccess(status) {
            var statuses = {};
            statuses[vm.experiment.fullId] = status;
            $rootScope.$broadcast('experiment-status-changed', statuses);
        }

        function selectTemplate(filename) {
            SignatureTemplates.query({}, function (result) {

                $uibModal.open({
                    animation: true,
                    templateUrl: 'scripts/app/entities/experiment/select-signature-template-modal/experiment-select-signature-template-modal.html',
                    controller: 'ExperimentSelectSignatureTemplateModalController',
                    controllerAs: 'vm',
                    resolve: {
                        result: function () {
                            return result;
                        }
                    }
                }).result.then(function (template) {
                    if (template) {
                        SignatureDocument.upload(
                            {
                                fileName: filename.fileName,
                                templateId: template.id,
                                experimentId: vm.experiment.id,
                                notebookId: vm.notebook.id,
                                projectId: vm.project.id
                            }, {},
                            function () {
                                onCompleteSuccess('Submitted');

                                var params = {
                                    projectId: vm.project.id,
                                    notebookId: vm.notebook.id,
                                    experimentId: vm.experiment.id
                                };

                                //reload experiment
                                EntitiesCache.removeByParams(params);
                                $state.go('entities.experiment-detail', params);
                            });
                    }
                });
            });
        }
    }
})();
