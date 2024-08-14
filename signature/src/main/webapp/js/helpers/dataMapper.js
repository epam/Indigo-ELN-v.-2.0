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
var DataMapper = {
    parseDateString: function(dateString){
        if(dateString == null){
            return null;
        }

        var inp = dateString.split("-");
        if(inp.length < 1){
            return null;
        }

        var d = inp[0].split(".");
        var m = (inp.length == 2) ? inp[1].split(".") : [0,0];

        if(d.length != 3){
            return null;
        }

        var year = parseInt(d[2]);
        var month = !isNaN(d[1]) ? parseInt(d[1])-1 : 1;
        var day = !isNaN(d[0]) ? parseInt(d[0]) : 1;
        var hours = !isNaN(m[0]) ? parseInt(m[0]) : 0;
        var mins = !isNaN(m[1]) ? parseInt(m[1]) : 0;

        return new Date(year, month, day, hours, mins, 0, 1);
    },

    transformDocument: function(indoc){
        var doc = {
            id: indoc.id,
            name: indoc.documentName,
            author: indoc.firstname + " " + indoc.lastname,
            creationDateText: indoc.creationDate,
            creationDate: DataMapper.parseDateString(indoc.creationDate),
            modifiedDateText: indoc.lastUpdateDate,
            modifiedDate: DataMapper.parseDateString(indoc.lastUpdateDate),
            statusId: parseInt(indoc.status),
            actionRequired: indoc.actionRequired,
            isViewed: indoc.inspected,
            witnesses: []
        };

        angular.forEach(indoc.witnesses, function(witness){
            doc.witnesses.push(DataMapper.transformWitness(witness));
        });

        return doc;
    },

    transformWitness: function(inWitness){
        var witness = {
           userName: inWitness.firstname + " " + inWitness.lastname,
           statusId: parseInt(inWitness.status),
           step: parseInt(inWitness.index),
           comment: inWitness.comment,
           actionDate: DataMapper.parseDateString(inWitness.actionDate),
           actionDateText: inWitness.actionDate
        };

        return witness;
    },

    transformTemplate: function(inTemplate){
        var templ = {
            id: inTemplate.id,
            name: inTemplate.name,
            author: inTemplate.author,
            signatureBlocks: []
        };
        angular.forEach(inTemplate.signatureBlocks, function(block){
            templ.signatureBlocks.push(DataMapper.transformSignatureBlock(block));
        });
        return templ;
    },

    transformSignatureBlock: function(inBlock){
        var block = {
           reasonId: parseInt(inBlock.reason),
           reason: null,
           index: parseInt(inBlock.index),
           userName: (inBlock.userfirstname + " " +inBlock.userlastname),
           user: {
               id: inBlock.userid,
               firstName: inBlock.userfirstname,
               lastName: inBlock.userlastname,
               login: inBlock.username
           }
        };

        return block;
    },

    transformReason: function(inReason){
        var reason = {
           id: inReason.id,
           name: inReason.name
        };

        return reason;
    },

    transformUser: function(inUser){
        var user = {
           id: inUser.userid,
           firstName: inUser.firstname,
           lastName: inUser.lastname,
           login: inUser.username,
           isActive: inUser.active,
           isAdmin: inUser.admin,
           email: inUser.email
        };

        return user;
    },

    getNewTemplate: function(){
        return {
            name: "",
            signatureBlocks: []
        }
    },

    getNewSignatureBlock: function(index){
        return {
           reasonId: 2,
           index: index,
           reason: null,
           user: null
        };
    },

    getNewUser: function(){
        return {
           id: null,
           firstName: '',
           lastName: '',
           login: '',
           isActive: true,
           isAdmin: false,
           password1: null,
           password2: null,
           email: ''
        }
    },

    transformRequestTemplate: function(inTemplate){
        var templ = {
            id: inTemplate.id,
            name: inTemplate.name,
            author: inTemplate.author,
            signatureBlocks: []
        };
        angular.forEach(inTemplate.signatureBlocks, function(block){
            templ.signatureBlocks.push(DataMapper.transformRequestSignatureBlock(block));
        });
        return templ;
    },

    transformRequestSignatureBlock: function(inBlock){
        var block = {
           userId: inBlock.user.id,
           reason: parseInt(inBlock.reason.id),
           index: parseInt(inBlock.index)
        };

        return block;
    },

    transformRequestUser: function(inUser){
        var user = {
           firstname: inUser.firstName,
           lastname: inUser.lastName,
           username: inUser.login,
           active: inUser.isActive,
           admin: inUser.isAdmin,
           password: inUser.password1,
           email: inUser.email
        };

        if(inUser.id){
            user.userid = inUser.id;
        }

        return user;
    }
};