'use strict';
angular.module('indigoeln')
    .directive('myScroller', function ($rootScope) {
        return {
            restrict: 'A',
            link: function (scope, iElement, iAttrs, controller ) {
                $(iElement).mCustomScrollbar({
                    axis:'x',
                    theme:'dark-thin',
                    callbacks:{
                        onUpdate:function(){
                            $(iElement).mCustomScrollbar('scrollTo','.active',{
                                scrollInertia: 500
                            });
                        }
                    }
                });
            }
        };
    });
