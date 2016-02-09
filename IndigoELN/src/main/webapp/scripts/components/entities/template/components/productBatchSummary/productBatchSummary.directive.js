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
                      { field: 'batchNbk', displayName : 'Nbk Batch #', enableCellEdit: false},
                      { 
                            field: 'totalWeight', displayName : 'Total Weight', 
                            editableCellTemplate: 'ui-grid/dropdownEditor', 
                            editDropdownValueLabel: 'label', editDropdownOptionsArray: [
                                { id: 10, label: 'ten' },
                                { id: 20, label: 'twenty' }
                            ]
                      },
                      { field: 'totalVolume', displayName : 'Total Volume'},
                      { field: 'totalMoles', displayName : 'Total Moles'},
                      { field: 'theoWgt', displayName : 'Theo. Wgt.'},
                      { field: 'theoMoles', displayName : 'Theo. Moles'}
                      /*{ field: 'f6', displayName : '%Yield'},
                      { field: 'f7', displayName : 'Compound State'},
                      { field: 'f8', displayName : 'Purity'},
                      { field: 'f9', displayName : 'Melting Point'},
                      { field: 'f10', displayName : 'Mol Wgt'}*/
                    ],
                    onRegisterApi : function( gridApi ) {
                    },
                    /*TODO replace with real data*/
                    data: [
                        {
                            "batchNbk": "1",
                            "totalWeight": "10",
                            "totalVolume": "100"
                        },
                        {
                            "batchNbk": "2",
                            "totalWeight": "20",
                            "totalVolume": "five hundred"
                        }
                        ]

                };
                
                angular.forEach(scope.batchSummaryOptions.columnDefs, function(columnDef) {
                	if (columnDef.enableCellEdit != false) {
                		columnDef.menuItems = [{
                                   title: 'Set value for ' + columnDef.displayName, 
                                   action: function($event) {
                                   }
                        }, {
                                   title: 'Set default units for ' + columnDef.displayName, 
                                   action: function($event) {
                                   }
                        }];
                	}
                });
            }
        };
    });