'use strict';

angular.module('indigoeln')
    .directive('productBatchSummary', function () {
        return {
            restrict: 'E',
            replace: true,
            scope: {
            },
            templateUrl: 'scripts/components/entities/template/components/productBatchSummary/productBatchSummary.html',
            link : function(scope, link, atrs) {
            	scope.batchSummaryOptions =  {
                    enableSorting: true,
                    enableGridMenu: true,
                    enableRowSelection: true,
                    enableSelectAll: true,
                    selectionRowHeaderWidth: 35,                    
                    columnDefs: [
                      { field: 'f1', displayName : 'Nbk Batch #', enableCellEdit: false},
                      { 
                            field: 'f2', displayName : 'Total Weight', 
                            editableCellTemplate: 'ui-grid/dropdownEditor', 
                            editDropdownValueLabel: 'label', editDropdownOptionsArray: [
                                { id: 10, label: 'ten' },
                                { id: 20, label: 'twenty' }
                            ]
                      },
                      { field: 'f3', displayName : 'Total Volume'}/*,
                      { field: 'f4', displayName : 'Total Moles'},
                      { field: 'f5', displayName : 'Theo. Wgt.'},
                      { field: 'f6', displayName : 'Theo. Moles'},
                      { field: 'f6', displayName : '%Yield'},
                      { field: 'f7', displayName : 'Compound State'},
                      { field: 'f8', displayName : 'Purity'},
                      { field: 'f9', displayName : 'Melting Point'},
                      { field: 'f10', displayName : 'Mol Wgt'}*/
                    ],
                    onRegisterApi : function( gridApi ) {
                    },
                    data: [
                        {
                            "Nbk Batch #": "Ethel Price",
                            "Total Weight": "female",
                            "www": "Enersol"
                        },
                        {
                            "Nbk Batch #": "Claudine Neal",
                            "Total Weight": "female",
                            "www": "Sealoud"
                        }
                        ]

                };
                
                angular.forEach(scope.batchSummaryOptions.columnDefs, function(columnDef) {
                	if (columnDef.enableCellEdit != false) {
                		columnDef.menuItems = [{
                                   title: 'Set value for ' + columnDef.displayName, 
                                   action: function($event) {
                                   }
                        }];
                	}
                });
            }
        };
    });