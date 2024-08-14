/****************************************************************************
 * Copyright (C) 2009-2015 EPAM Systems
 *
 * This file is part of Indigo Signature Service.
 *
 * This file may be distributed and/or modified under the terms of the
 * GNU General Public License version 3 as published by the Free Software
 * Foundation and appearing in the file LICENSE.GPL included in the
 * packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
 * WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 ***************************************************************************/
'use strict';

/* Directives */

angular.module('App.directives', [])
.directive('statusText', function() {
	    return {
	        restrict: 'A',
	        priority:1,
	        link: function(scope, elm, attr) {

	            attr.$observe('statusText', function(val) {
                    var text = "";
                    var status = isNaN(val) ? 0 : parseInt(val);
                    switch(status){
                        case 1:
                            text = "Submitted";
                        break;
                        case 2:
                            text = "Signing";
                        break;
                        case 3:
                            text = "Signed";
                        break;
                        case 4:
                            text = "Rejected";
                        break;
                        case 5:
                            text = "Waiting";
                        break;
                        case 6:
                            text = "Cancelled";
                            break;
                        case 7:
                            text = "Archiving";
                        break;
                        case 8:
                            text = "Archived";
                        break;
                    }
                    elm.html(text);
	            });
	        }
	    };
	})
.directive('statusIcon', function() {
        return {
            restrict: 'A',
            priority:1,
            link: function(scope, elm, attr) {

                attr.$observe('statusIcon', function(val) {
                    var classText, classTitle = "";
                    var status = isNaN(val) ? 0 : parseInt(val);
                    switch(status){
                        case 1:
                            classText = "submitted";
                            classTitle = "Submitted";
                        break;
                        case 2:
                            classText = "signing";
                            classTitle = "Signing";
                        break;
                        case 3:
                              classText = "signed";
                              classTitle = "Signed";
                          break;
                        case 4:
                            classText = "rejected";
                            classTitle = "Rejected";
                        break;
                        case 5:
                            classTitle = "Waiting";
                            classText = "waiting";
                        break;
                        case 6:
                            classTitle = "Cancelled";
                            classText = "cancelled";
                            break;
                        case 7:
                            classTitle = "Archiving";
                            classText = "archiving";
                        break;
                        case 8:
                            classTitle = "Archived";
                            classText = "archived";
                        break;
                    }
                    elm.attr("title", classTitle);
                    elm.removeClass("statuses submitted signing signed rejected waiting archiving archived cancelled");
                    elm.addClass("statuses "+classText);
                });
            }
        };
    })
.directive('focusOn', function() {
   return function(scope, elem, attr) {
      scope.$on(attr.focusOn, function(e) {
          elem[0].focus();
      });
   };
})
.directive('ngEnter', function() {
      return function(scope, element, attrs) {
          element.bind("keydown keypress", function(event) {
              if(event.which === 13) {
                  scope.$apply(function(){
                      scope.$eval(attrs.ngEnter);
                  });

                  event.preventDefault();
              }
          });
      }
  })
  .directive('validateUser', function (){
     return {
        require: 'ngModel',
        link: function(scope, elem, attr, ngModel) {

        scope.$watch(function () {
                  return ngModel.$modelValue;
               }, function(newValue) {
                   var valid = newValue && newValue.id && newValue.id!=0;
                   ngModel.$setValidity('validateUser', valid);
               });
        }
     };
  })
  .directive('validatePassword', function (){
     return {
        require: 'ngModel',
        link: function(scope, elem, attr, ngModel) {
            function check(value){
                return (value && value != '') ? value.length >=6 && value.match(/^(?=.*[A-Z])(?=.*\d).*$/)  : true;
            }

            //For DOM -> model validation
            ngModel.$parsers.unshift(function(value) {
               var valid = check(value);
               ngModel.$setValidity('validatePassword', valid);
               return valid ? value : undefined;
            });

            //For model -> DOM validation
            ngModel.$formatters.unshift(function(value) {
               ngModel.$setValidity('validatePassword', check(value));
               return value;
            });
        }
     };
  })
  .directive("validateComparedPassword", function($parse) {
      return {
          require: "ngModel",
          link: function(scope, elem, attrs, ctrl) {
              scope.$watch(function() {
                return $parse(attrs.validateComparedPassword)(scope) === ctrl.$modelValue;
              }, function(currentValue) {
                ctrl.$setValidity('validateComparedPassword', currentValue);
              });
          }
      };
  })
  .directive('autoFillSync', function($timeout) {
     return {
        require: 'ngModel',
        link: function(scope, elem, attrs, ngModel) {
            var origVal = elem.val();
            $timeout(function () {
                var newVal = elem.val();
                if(ngModel.$pristine && origVal !== newVal) {
                    ngModel.$setViewValue(newVal);
                }
            }, 500);
        }
     }
  });