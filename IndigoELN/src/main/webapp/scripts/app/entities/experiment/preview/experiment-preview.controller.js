(function () {
    angular
        .module('indigoeln')
        .controller('ExperimentPreviewController', ExperimentPreviewController);

    /* @ngInject */
    function ExperimentPreviewController($state, $rootScope, $uibModal, pageInfo,
                                         experimentPdfCreator, SignatureTemplates, SignatureDocument, EntitiesCache) {
        var self = this;

        self.fullPrint = $state.current.data.isFullPrint;
        self.experiment = pageInfo.experiment;
        self.notebook = pageInfo.notebook;
        self.project = pageInfo.project;

        self.batchDetails = self.experiment.components.productBatchDetails;
        self.batchSummary = self.experiment.components.productBatchSummary;
        self.conceptDetails = self.experiment.components.conceptDetails;
        self.reactionDetails = self.experiment.components.reactionDetails;
        self.reaction = self.experiment.components.reaction;
        self.molecule = self.experiment.components.molecule;
        self.experimentDescription = self.experiment.components.experimentDescription;
        self.stoichTable = self.experiment.components.stoichTable;
        self.preferredCompounds = self.experiment.components.preferredCompoundSummary;
        self.currentDate = Date.now();

        self.print      = print;
        self.toDigits   = toDigits;
        self.joinArr    = joinArr;
        self.submit     = submit;

        if (self.batchSummary) {
            self.registrationSummary = {
                batches: self.batchSummary.batches.filter(function (b) {
                    return b.conversationalBatchNumber;
                })
            };
        }

        function print() {
            self.isPrinting = true;
            var fileName = self.notebook.name + '-' + self.experiment.name;
            if (self.experiment.experimentVersion > 1 || !self.experiment.lastVersion) {
                fileName += ' v' + self.experiment.experimentVersion;
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
            var fileName = self.notebook.name + '-' + self.experiment.name;
            if (self.experiment.experimentVersion > 1 || !self.experiment.lastVersion) {
                fileName += ' v' + self.experiment.experimentVersion;
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
            self.isPrinting = false;
        }

        function onCompleteSuccess(status) {
            var statuses = {};
            statuses[self.experiment.fullId] = status;
            $rootScope.$broadcast('experiment-status-changed', statuses);
        }

        function selectTemplate(filename) {
            SignatureTemplates.query({}, function (result) {

                $uibModal.open({
                    animation: true,
                    templateUrl: 'scripts/app/entities/experiment/select-signature-template-modal/experiment-select-signature-template-modal.html',
                    controller: 'ExperimentSelectSignatureTemplateModalController',
                    controllerAs: 'experimentSelectSignatureTemplateModalController',
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
                                experimentId: self.experiment.id,
                                notebookId: self.notebook.id,
                                projectId: self.project.id
                            }, {},
                            function () {
                                onCompleteSuccess('Submitted');

                                var params = {
                                    projectId: self.project.id,
                                    notebookId: self.notebook.id,
                                    experimentId: self.experiment.id
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
