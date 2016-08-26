angular.module('indigoeln')
    .controller('SearchPanelController', function ($rootScope, $scope, $state, Dashboard, CONFIG) {


        $scope.model = {};
        $scope.model.restrictions = {
            searchQuery: '',
            advancedSearch: {
                therapeuticArea: {name: 'Therapeutic Area', field: 'therapeuticArea', condition: {name: 'contains'}, conditionList : [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]},
                projectCode: {name: 'Project Code', field: 'projectCode', condition: {name: 'contains'}, conditionList : [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]},
                batchYield: {name: 'Batch Yield%', field: 'batchYield', condition: {name: '>'}, conditionList : [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]},
                batchPurity: {name: 'Batch Purity%', field: 'batchPurity', condition: {name: '>'}, conditionList : [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]},
                subject: {name: 'Subject/Title', field: 'subject', condition: {name: 'contains'}, conditionList : [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]},
                entityDescription: {name: 'Entity Description', field: 'entityDescription', condition: {name: 'contains'}, conditionList : [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]},
                compoundId: {name: 'Compound ID', field: 'compoundId', condition: {name: 'contains'}, conditionList : [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]},
                literatureRef: {name: 'Literature Ref', field: 'literatureRef', condition: {name: 'contains'}, conditionList : [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]},
                entityKeywords: {name: 'Entity Keywords', field: 'entityKeywords', condition: {name: 'contains'}, conditionList : [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]},
                chemicalName: {name: 'Chemical Name', field: 'chemicalName', condition: {name: 'contains'}, conditionList : [
                    {name: 'contains'}, {name: 'starts with'}, {name: 'ends with'}, {name: 'between'}
                ]}
            },
            structure: {
                name: 'Reaction Scheme',
                similarityCriteria: {name: 'equal'},
                similarityValue: null,
                image: null,
                type: {name: 'Product Or Reaction'}
            }
        };


        $scope.structureTypes = [{name:'Reaction'},{name:'Product'}, {name:'Product Or Reaction'}];
        $scope.conditionSimilarity = [{name: 'equal'}, {name: 'substructure'}, {name: 'similarity'}];

    });
