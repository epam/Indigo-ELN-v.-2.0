/**
 * Created by Stepan_Litvinov on 1/25/2016.
 */
/* globals $ */
'use strict';
angular.module('indigoeln')
    .config(function ($builderProvider) {
        $builderProvider.registerComponent('sampleInput', {
            group: 'from html',
            label: 'Sample',
            description: 'From html template',
            placeholder: 'placeholder',
            required: false,
            validationOptions: [
                {
                    label: 'none',
                    rule: '/.*/'
                }, {
                    label: 'number',
                    rule: '[number]'
                }, {
                    label: 'email',
                    rule: '[email]'
                }, {
                    label: 'url',
                    rule: '[url]'
                }
            ],
            templateUrl: 'scripts/components/form/templates/pnbs/template.html'
        });
    });