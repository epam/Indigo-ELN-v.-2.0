angular.module('indigoeln').controller('ExperimentSubmitController',
    function ($scope, $rootScope, $state, pageInfo, experimentPdfCreator,
              SignatureTemplates, $uibModal, SignatureDocument, EntitiesCache) {
        var vm = this;

        vm.previewTitle = 'Preview Experiment';
        vm.confidentalText = 'CONFIDENTIAL';
        vm.authorText = 'Author:';
        vm.notebookExperimentText = 'Notebook&nbsp;Experiment:';
        vm.projectText = 'Project:';
        vm.statusText = 'Status:';
        vm.printedPageText = 'Printed Page:';
        vm.printedDateText = 'Printed Date:';
        vm.subjectTitleText = 'Subject/Title:';
        vm.printText = 'Print';
        vm.reactionDetailsText = 'REACTION DETAILS';
        vm.creationDateText = 'Creation Date:';
        vm.therapeuticAreaText = 'Therapeutic&nbsp;Area:';
        vm.continuedFromText = 'Continued From:';
        vm.projectCodeText = 'Project Code:';
        vm.continuedToText = 'Continued To:';
        vm.projectAliasText = 'Project Alias';
        vm.linkedExperimentText = 'Linked&nbsp;Experiment:';
        vm.literatureReferenceText = 'Literature&nbsp;Reference:';
        vm.coAuthorsText = 'Co-authors:';
        vm.reactionSchemeText = 'REACTION SCHEME';
        vm.imageAnavailableAltText = 'Image is unavailable.';
        vm.printLabelText = 'Print';
        vm.stoichiometryText = 'STOICHIOMETRY';
        vm.printTableReagentText = 'Reagent';
        vm.printTableMolWghText = 'Mol Wgh';
        vm.printTableWeightText = 'Weight';
        vm.printTableMolesText = 'Moles';
        vm.printTableVolumeText = 'Volume';
        vm.printTableEQText = 'EQ';
        vm.printTableOtherInformationText = 'Other Information';
        vm.nameText = 'Name:';
        vm.rxnRoleText = 'Rxn Role:';
        vm.purityText = 'Purity:';
        vm.molarityText = 'Molarity:';
        vm.hazardText = 'Hazard:';
        vm.commentsText = 'Comments:';
        vm.saltCodeText = 'Salt Code:';
        vm.saltEQText = 'Salt EQ:';
        vm.experimentDescriptionText = 'EXPERIMENT DESCRIPTION';
        vm.batchInformationText = 'BATCH INFORMATION';
        vm.nbkBatchText = 'Nbk Batch';
        vm.structureText = 'Structure';
        vm.amountMadeText = 'Amount Made';
        vm.theoreticalYieldText = 'Theoretical Yield';
        vm.purityDeterminedByText = 'Purity (%) Determined By';
        vm.batchInformationText = 'Batch Information';
        vm.molWgtText = 'Mol Wgt:';
        vm.exactMassText = 'Exact Mass:';
        vm.saltCodeText = 'Salt Code:';
        vm.saltEQText = 'Salt EQ:';
        vm.batchOwnerText = 'Batch Owner:';
        vm.registrationSummaryText = 'REGISTRATION SUMMARY';
        vm.totalAmountMadeText = 'Total Amount Made';
        vm.registrationStatusText = 'Registration Status';
        vm.conversationalBatchText = 'Conversational Batch';
        vm.batchDetailsText = 'BATCH DETAILS';
        vm.forNotebookBatchText = 'For Notebook Batch:';
        vm.degisteredDateText = 'Registered Date';
        vm.structureCommentsText = 'Structure Comments';
        vm.compoundSourceText = 'Compound Source';
        vm.sourceDetailText = 'Source Detail';
        vm.batchOwnerText = 'Batch Owner';
        vm.calculatedBatchMWText = 'Calculated Batch MW';
        vm.calculatedBatchMFText = 'Calculated Batch MF';
        vm.residualSolventsText = 'Residual Solvents';
        vm.solubilityInSolventsText = 'Solubility in Solvents';
        vm.precursorReactantIDsText = 'Precursor/Reactant IDs';
        vm.externalSupplierText = 'External Supplier';
        vm.hazardsText = 'Hazards';
        vm.handlingText = 'Handling';
        vm.storageText = 'Storage';
        vm.conceptDetailsText = 'CONCEPT DETAILS';
        vm.conceptKeywordsText = 'Concept Keywords:';
        vm.designersText = 'Designers';
        vm.preferredCompoundsText = 'PREFERRED COMPOUNDS';
        vm.structureText = 'Structure';
        vm.notebookBatchNText = 'Notebook Batch #';
        vm.molWeightText = 'Mol Weight';
        vm.molFormulaText = 'Mol Formula';
        vm.structureCommentsText = 'Structure Comments';
        vm.imageIsUnavailableAltText = 'Image is unavailable.';
        vm.submitText = 'Submit';
        vm.backText = 'Back';

        $scope.fullPrint = true;

        $scope.experiment = pageInfo.experiment;
        $scope.notebook = pageInfo.notebook;
        $scope.project = pageInfo.project;

        $scope.batchDetails = $scope.experiment.components.productBatchDetails;
        $scope.batchSummary = $scope.experiment.components.productBatchSummary;
        $scope.conceptDetails = $scope.experiment.components.conceptDetails;
        $scope.reactionDetails = $scope.experiment.components.reactionDetails;
        $scope.reaction = $scope.experiment.components.reaction;
        $scope.molecule = $scope.experiment.components.molecule;

        $scope.currentDate = Date.now();

        var onCompleteSuccess = function (status) {
            var statuses = {};
            statuses[$scope.experiment.fullId] = status;
            $rootScope.$broadcast('experiment-status-changed', statuses);
        };

        var selectTemplate = function (filename) {
            SignatureTemplates.query({}, function (result) {

                $uibModal.open({
                    animation: true,
                    templateUrl: 'scripts/app/entities/experiment/experiment-select-signature-template-modal.html',
                    controller: function ($scope, $uibModalInstance) {
                        $scope.items = result.Templates;
                        $scope.dismiss = function () {
                            $uibModalInstance.dismiss('cancel');
                        };
                        $scope.selectSignatureTemplateModal = function () {
                            $uibModalInstance.close($scope.selectedTemplate);
                        };
                    }
                }).result.then(function (template) {
                    if (template) {
                        SignatureDocument.upload(
                            {
                                fileName: filename.fileName,
                                templateId: template.id,
                                experimentId: $scope.experiment.id,
                                notebookId: $scope.notebook.id,
                                projectId: $scope.project.id
                            }, {},
                            function () {
                                onCompleteSuccess('Submitted');

                                var params = {
                                    projectId: $scope.project.id,
                                    notebookId: $scope.notebook.id,
                                    experimentId: $scope.experiment.id
                                };

                                //reload experiment
                                EntitiesCache.removeByParams(params);
                                $state.go('entities.experiment-detail', params);
                            });
                    }
                });
            });
        };

        $scope.submit = function () {
            var fileName = $scope.notebook.name + '-' + $scope.experiment.name;
            if ($scope.experiment.experimentVersion > 1 || !$scope.experiment.lastVersion) {
                fileName += ' v' + $scope.experiment.experimentVersion;
            }
            experimentPdfCreator.createPDF(fileName, selectTemplate);
        };

    });
