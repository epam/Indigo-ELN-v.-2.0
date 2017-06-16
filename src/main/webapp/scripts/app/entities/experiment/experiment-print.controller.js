angular.module('indigoeln').controller('ExperimentPrintController',
    function ($scope, pageInfo, experimentPdfCreator) {
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

        $scope.fullPrint = false;

        $scope.experiment = pageInfo.experiment;
        $scope.notebook = pageInfo.notebook;
        $scope.project = pageInfo.project;

        $scope.batchDetails = $scope.experiment.components.productBatchDetails;
        $scope.batchSummary = $scope.experiment.components.productBatchSummary;
        $scope.conceptDetails = $scope.experiment.components.conceptDetails;
        $scope.reactionDetails = $scope.experiment.components.reactionDetails;
        $scope.reaction = $scope.experiment.components.reaction;
        $scope.molecule = $scope.experiment.components.molecule;
        $scope.experimentDescription = $scope.experiment.components.experimentDescription;
        $scope.stoichTable = $scope.experiment.components.stoichTable;
        $scope.preferredCompounds = $scope.experiment.components.preferredCompoundSummary;
        if ($scope.batchSummary)
            $scope.registrationSummary = {batches : $scope.batchSummary.batches.filter(function(b) { return b.conversationalBatchNumber}) }

        $scope.currentDate = Date.now();

        var downloadReport = function (response) {
            var hiddenFrameId = 'hiddenDownloader';
            var hiddenDownloader = $('#' + hiddenFrameId);
            if (!hiddenDownloader.length) {
                hiddenDownloader = $('<iframe id="' + hiddenFrameId + '" style="display:none"/>');
                $('body').append(hiddenDownloader);
            }
            hiddenDownloader.attr('src', 'api/print?fileName=' + response.fileName);
            $scope.isPrinting = false;
        };

        $scope.print = function () {
            $scope.isPrinting = true;
            var fileName = $scope.notebook.name + '-' + $scope.experiment.name;
            if ($scope.experiment.experimentVersion > 1 || !$scope.experiment.lastVersion) {
                fileName += ' v' + $scope.experiment.experimentVersion;
            }
            experimentPdfCreator.createPDF(fileName, downloadReport);
        };
        $scope.toDigits = function(o) {
            if (!o || o.value == 0) return '';
            var val = o.value ? Number(o.value) : Number(o);
            if (!val) return '';
            var unit = o.unit ? ' ' + o.unit : ''; 
            return val.toFixed(3).replace(/0+$/,'').replace(/\.+$/,'')+ unit;
        }
        $scope.joinArr = function(users, fi) {
            return users ? users.map(function(u) { return fi ? u[fi] : u.name }).join(', ') : null;
        }
        console.warn($scope.experiment)

    });
