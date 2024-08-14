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

/* Filters */

angular.module('App.filters', [])
.filter('filterDocuments', function() {
      return function(documents, name, time, status, showAll) {
            var today = new Date();
            var lastYear = new Date(today.getYear()-1, today.getMonth(), today.getDate());
            var lastMonth = (today.getMonth() > 0)
                ? new Date(today.getYear(), today.getMonth()-1, today.getDate())
                : new Date(today.getYear()-1, 11, today.getDate());
            var milliDay = 1000*60*60*24;

            function checkText(text, document){
                text = text.toLowerCase();
                var isName = document.name.toLowerCase().indexOf(text) != -1;
                var isAuthor = document.author.toLowerCase().indexOf(text) != -1;
                var isWitness = false;
                angular.forEach(document.witnesses, function(witness){
                    if(witness.userName.toLowerCase().indexOf(text) != -1){
                        isWitness = true;
                    }
                });

                return isName || isAuthor || isWitness;
            }

            function check(document){
                if(showAll){
                    if(status != null && status.id != document.statusId){
                        return false;
                    }
                }
                else{
                    if(!document.actionRequired){
                        return false;
                    }
                    if(status != null && status.id != document.statusId){
                        return false;
                    }
                    if(status == null && document.statusId != 1 && document.statusId != 2){
                        return false;
                    }
                }
                if(name != "" && !checkText(name, document)){
                    return false;
                }
                if(time != null){
                    if(time.id == 1 && document.creationDate < lastYear){
                        //year
                        return false;
                    }
                    if(time.id == 2 && document.creationDate < lastMonth){
                        //month
                        return false;
                    }
                    if(time.id == 3 && (today - document.creationDate)/milliDay > 7){
                        //week
                        return false;
                    }
                    if(time.id == 4 && (today - document.creationDate)/milliDay > 1){
                        //day
                        return false;
                    }
                }

                return true;
            }

            var res = [];

            angular.forEach(documents, function(document){
                if(check(document)){
                    res.push(document);
                }
            });

            return res;
      };
})
;