/*
 * Copyright (C) 2015-2018 EPAM Systems
 *
 * This file is part of Indigo ELN.
 *
 * Indigo ELN is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Indigo ELN is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Indigo ELN.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

var indigoAttachments = require('./directives/attachments/indigo-attachments.directive');
var indigoBatchStructure = require('./directives/batch-structure/indigo-batch-structure.directive');
var indigoComponents = require('./directives/indigo-components/indigo-components.directive');
var indigoPreferredCompoundDetails =
    require('./directives/prefer-compound-details/indigo-prefer-compound-details.directive');
var indigoPreferredCompoundsSummary =
    require('./directives/prefer-compounds-summary/indigo-prefer-compound-summary.directive');
var indigoProductBatchDetails = require('./directives/product-batch-details/indigo-product-batch-details.directive');
var indigoProductBatchSummary = require('./directives/product-batch-summary/indigo-product-batch-summary.directive');
var indigoReactionScheme = require('./directives/reaction-scheme/indigo-reaction-scheme.directive');
var indigoBatchSummary = require('./common/batch-summary/indigo-batch-summary.directive');
var indigoCompoundSummary = require('./common/compound-summary/indigo-compound-summary.directive');
var indigoInlineLoader = require('./common/inline-loader/indigo-inline-loader.directive');
var indigoConceptDetails = require('./directives/concept-details/indigo-concept-details.directive');
var indigoReactionDetails = require('./directives/reaction-details/indigo-reaction-details.directive');
var indigoExperimentDescription =
    require('./directives/experiment-description/indigo-experiment-description.directive');
var indigoSearchResultTable = require('./common/search-result-table/indigo-search-result-table.directive');

var indigoStoichTable = require('./directives/stoich-table/stoich-table.module');
var componentButtons = require('./directives/component-buttons/component-buttons.module.js');
var structureScheme = require('./common/structure-scheme/structure-scheme.module');
var editInfoPopup = require('./common/edit-info-popup/edit-info-popup.module');
var indigoTable = require('./common/table/indigo-table.module');

var ProductBatchSummarySetSourceController =
    require('./directives/product-batch-summary-set-source/product-batch-summary-set-source.controller');
var AnalyzeRxnController = require('./common/analyze-rxn/analyze-rxn.controller');
var EntitiesToSaveController = require('./services/dialog-service/entities-to-save/entities-to-save.controller');
var StructureValidationController =
    require('./services/dialog-service/structure-validation/structure-validation.controller');
var SearchReagentsController = require('./common/search-reagents/search-reagents.controller');
var SomethingDetailsController = require('./common/something-details/something-details.controller');

var typeOfComponents = require('./constants/type-of-components.constant');
var searchReagentsService = require('./common/search-reagents/search-reagents.service');

var productBatchSummaryCache = require('./services/product-batch-summary-cache.service');
var productBatchSummaryOperations = require('./services/product-batch-summary-operations.service');
var batchHelper = require('./services/batch-helper.service');
var columnActions = require('./services/column-actions.service');
var dialogService = require('./services/dialog-service/dialog.service');
var calculationHelper = require('./services/calculation/calculation-helper.service');
var batchesCalculation = require('./services/batches-calculation/batches-calculation.service');
var componentsUtil = require('./utils/components-util.service');

var run = require('./indigo-components.run');

var dependencies = [
    indigoStoichTable,
    componentButtons,
    structureScheme,
    editInfoPopup,
    indigoTable
];

module.exports = angular
    .module('indigoeln.indigoComponents', dependencies)

    .directive('indigoSearchResultTable', indigoSearchResultTable)
    .directive('indigoAttachments', indigoAttachments)
    .directive('indigoBatchStructure', indigoBatchStructure)
    .directive('indigoComponents', indigoComponents)
    .directive('indigoPreferredCompoundDetails', indigoPreferredCompoundDetails)
    .directive('indigoPreferredCompoundsSummary', indigoPreferredCompoundsSummary)
    .directive('indigoProductBatchDetails', indigoProductBatchDetails)
    .directive('indigoProductBatchSummary', indigoProductBatchSummary)
    .directive('indigoReactionScheme', indigoReactionScheme)
    .directive('indigoBatchSummary', indigoBatchSummary)
    .directive('indigoCompoundSummary', indigoCompoundSummary)
    .directive('indigoInlineLoader', indigoInlineLoader)
    .directive('indigoConceptDetails', indigoConceptDetails)
    .directive('indigoExperimentDescription', indigoExperimentDescription)
    .directive('indigoReactionDetails', indigoReactionDetails)

    .controller('ProductBatchSummarySetSourceController', ProductBatchSummarySetSourceController)
    .controller('AnalyzeRxnController', AnalyzeRxnController)
    .controller('EntitiesToSaveController', EntitiesToSaveController)
    .controller('StructureValidationController', StructureValidationController)
    .controller('SearchReagentsController', SearchReagentsController)
    .controller('SomethingDetailsController', SomethingDetailsController)

    .constant('typeOfComponents', typeOfComponents)

    .factory('productBatchSummaryCache', productBatchSummaryCache)
    .factory('productBatchSummaryOperations', productBatchSummaryOperations)
    .factory('batchHelper', batchHelper)
    .factory('columnActions', columnActions)
    .factory('dialogService', dialogService)
    .factory('calculationHelper', calculationHelper)
    .factory('batchesCalculation', batchesCalculation)
    .factory('componentsUtil', componentsUtil)
    .factory('searchReagentsService', searchReagentsService)

    .run(run)

    .name;
