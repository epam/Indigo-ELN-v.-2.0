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

/* Services */

angular.module('App.services', ['ngResource'])
.factory('dataFactory', ['$http', '$upload',
                function($http, $upload) {

	var service = {
		getDocuments: function(cb) {
            return $http.get('getDocuments'+'?noCache=' + new Date().getTime()).success(function(data){
                var res = [];

                angular.forEach(data.Documents, function(indoc){
                    var doc = DataMapper.transformDocument(indoc);

                    res.push(doc);
                });

                if(cb){
                    cb(res);
                }
            });

		},

		GetDocumentInfo: function(documentId, cb) {
            var paramsData = {
                id: documentId
                };

            return $http.get('getDocumentInfo', {params: paramsData}).success(function(data){
                 var doc = DataMapper.transformDocument(data);

                if(cb){
                    cb(doc);
                }
            });

		},

        uploadDocument: function(file, templateId, progressCallback, successCallback, errorCallback) {
            return $upload.upload({
                url: 'uploadDocument', //upload.php script, node.js route, or servlet url
                // method: POST or PUT,
                // headers: {'headerKey': 'headerValue'},
                // withCredential: true,
                data: {templateId: templateId},
                file: file
                // file: $files, //upload multiple files, this feature only works in HTML5 FromData browsers
                /* set file formData name for 'Content-Desposition' header. Default: 'file' */
                //fileFormDataName: myFile, //OR for HTML5 multiple upload only a list: ['name1', 'name2', ...]
                /* customize how data is added to formData. See #40#issuecomment-28612000 for example */
                //formDataAppender: function(formData, key, val){}
                }).progress(function(evt) {
                    //console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
                    if(progressCallback){
                        progressCallback(evt);
                    }
                }).success(function(data, status, headers, config) {
                    // file is uploaded successfully
                    if(data.error != null){
                        if(errorCallback){
                            errorCallback(data.error.text);
                        }
                    }
                    else{
                        var doc = DataMapper.transformDocument(data);

                        if(successCallback){
                            successCallback(doc);
                        }
                    }

            });
        },

        getTemplates: function(cb){
           return $http.get('getTemplates').success(function(data){
               var res = [];

               angular.forEach(data.Templates, function(tmpl){
                   var t = DataMapper.transformTemplate(tmpl);

                   res.push(t);
               });

               if(cb){
                   cb(res);
               }
           });
        },
        
        signDocumentVerizon: function(documentId, password, comment, successCallback, errorCb){
        	var paramsData = {
                    id: documentId,
                    comment: comment || "",
                    password: password
                };

            return $http({
                method: 'POST',
                url: 'signVerizon3Legged',
                data: $.param(paramsData),
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).success(function(data){
                if(data.error != null){
                    if(errorCb){
                        errorCb(data.error);
                    }
                }
                else{
                    var doc = DataMapper.transformDocument(data);

                    if(cb){
                        cb(doc);
                    }
                }
            });
            /*
            return $upload.upload({
                url: 'signVerizon3Legged',
                data: paramsData
            }).progress(function(evt) {

            }).success(function(data, status, headers, config) {
                if(data.error != null){
                    if(errorCb){
                        errorCb(data.error);
                    }
                    else{
                        var doc = DataMapper.transformDocument(data);

                        if(successCallback){
                            successCallback(doc);
                        }
                    }
                }
            });
            */
        },

        checkVerizonStatus: function(documentId, successCallback, errorCb){
            var paramsData = {
                documentid: documentId
            };

            return $http({
                method: 'POST',
                url: 'checkVerizonComplete',
                data: $.param(paramsData),
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            }).success(function(data){
                if(data.error != null){
                    if(errorCb){
                        errorCb(data.error);
                    }
                }
                else{
                    var doc = DataMapper.transformDocument(data);

                    if(successCallback){
                        successCallback(doc);
                    }
                }
            });
        },
        
        signDocument: function(documentId, password, comment, file, progressCallback, successCallback, errorCb){
            var paramsData = {
                id: documentId,
                comment: comment || "",
                password: password
            };
            return $upload.upload({
                            url: 'signDocument',
                            data: paramsData,
                            file: file
                            }).progress(function(evt) {
                                if(progressCallback){
                                    progressCallback(evt);
                                }
                            }).success(function(data, status, headers, config) {
                                 if(data.error != null){
                                     if(errorCb){
                                        errorCb(data.error.text);
                                     }
                                 }
                                 else{
                                     var doc = DataMapper.transformDocument(data);

                                     if(successCallback){
                                        successCallback(doc);
                                     }
                                 }
                            });
        },

        rejectDocument: function(documentId, comment, cb, errorCb){
            var paramsData = {
                id: documentId,
                comment: comment || ""
            };

            return $http.post('rejectDocument', paramsData).success(function(data){
                 if(data.error != null){
                     if(errorCb){
                        errorCb(data.error.text);
                     }
                 }
                 else{
                     var doc = DataMapper.transformDocument(data);

                    if(cb){
                        cb(doc);
                    }
                }
            });
        },

        saveTemplate: function(template, cb){
            var paramsData = DataMapper.transformRequestTemplate(template);
            return $http.post('updateTemplate', paramsData).success(function(data){
                 var templ = DataMapper.transformTemplate(data);

                if(cb){
                    cb(templ);
                }
            });
        },

        deleteTemplate: function(templateId, cb, errorCb){
            var paramsData = {
                id: templateId
            };
            return $http.get('removeTemplate', {params: paramsData}).success(function(data){
                if(data.error != null){
                    if(errorCb){
                       errorCb(data.error.text);
                    }
                }
                else{
                    if(cb){
                        cb();
                    }
                    }
            });
        },

        createTemplate: function(template, cb){
            var paramsData = DataMapper.transformRequestTemplate(template);
            return $http.post('createTemplate', paramsData).success(function(data){
                 var templ = DataMapper.transformTemplate(data);

                if(cb){
                    cb(templ);
                }
            });
        }
    };

    service.getReasons = function(cb) {
            return $http.get('getReasons').success(function(data){
                var res = [];

                angular.forEach(data.Reasons, function(indoc){
                    var reas = DataMapper.transformReason(indoc);

                    res.push(reas);
                });

                if(cb){
                    cb(res);
                }
            });

        };

	return service;

}])
.factory('userFactory', function ($http) {
    var service = {
        find: function(anr, limit, cb) {
            //delete $http.defaults.headers.common['X-Requested-With'];
            var paramsData = {
                limit: limit || 0,
                particle: anr
                };

            return $http.get("findUsers", {params:paramsData}).success(function(data){
                var res = [];

                angular.forEach(data.Users, function(inUser){
                    var user = DataMapper.transformUser(inUser);

                    res.push(user);
                });

                if(cb){
                    cb(res);
                }
                return res;
            });

        },

        search: function(anr) {
            //delete $http.defaults.headers.common['X-Requested-With'];
            var paramsData = {
                limit: 8,
                particle: anr
                };

            return $http.get("findUsers", {params:paramsData}).then(function(response){
                var res = [];

                angular.forEach(response.data.Users, function(inUser){
                    var user = DataMapper.transformUser(inUser);

                    res.push(user);
                });

                return res;
            });

        },

        createUser: function(user, cb, errorCb){
             var paramsData = DataMapper.transformRequestUser(user);
             return $http.post('createUser', paramsData).success(function(data){
                    if(data.error != null){
                      if(errorCb){
                         errorCb(data.error.text);
                      }
                    }
                    else{
                         var newUser = DataMapper.transformUser(data);

                         if(cb){
                             cb(newUser);
                         }
                    }
             });
        },

         saveUser: function(user, cb, errorCb){
              var paramsData = DataMapper.transformRequestUser(user);
              return $http.post('updateUser', paramsData).success(function(data){
                     if(data.error != null){
                       if(errorCb){
                          errorCb(data.error.text);
                       }
                     }
                     else{
                          var newUser = DataMapper.transformUser(data);

                          if(cb){
                              cb(newUser);
                          }
                     }
              });
         },
        doLogin: function(login, password, cb, errorCb){
            var paramsData = {
                username: login,
                password: password
            };
            // !!! use jQuery post to send application/x-www-form-urlencoded
            return $.post('login', paramsData).done(function(data){
                    if(data.loggedIn){
                        var newUser = DataMapper.transformUser(angular.fromJson(data.user));
                        if(cb){
                            cb(newUser);
                        }
                    }else{
                        if(errorCb){
                            errorCb();
                        }
                    }
            }).fail(function(data, status, headers, config){
                console.log("!!! auth fail", data, status, headers, config);
                if(status == 403){
                    if(errorCb){
                        errorCb();
                    }
                }
            });
        },
        doLogout: function(){
                    return $http.get('logout');
        }
    };

    service.getUser = function(cb, errorCb){
        return $http.get('getUser').success(function(data){
                if(data){
                    var newUser = DataMapper.transformUser(data);
                    if(cb){
                        cb(newUser);
                    }
                }else{
                    if(errorCb){
                        errorCb();
                    }
                }
        }).error(function(data, status, headers, config){
              if(errorCb){
                  errorCb();
              }
      });
    };

    return service;
});
