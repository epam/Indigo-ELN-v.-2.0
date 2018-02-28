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

/* @ngInject */
function sdImportHelper(appValuesService) {
    var additionalFormatFunctions = {
        GLOBAL_SALT_CODE: function(property, value) {
            return getItem(appValuesService.getSaltCodeValues(), property.subPropName, value);
        },
        GLOBAL_SALT_EQ: function(property, value) {
            return {
                value: parseInt(value, 10),
                entered: false
            };
        },
        STRUCTURE_COMMENT: function(property, value) {
            return value;
        },
        BATCH_COMMENTS: function(property, value) {
            return value;
        },
        BATCH_TYPE: function(property, value) {
            return value;
        }
    };

    return {
        additionalFormatFunctions: additionalFormatFunctions,
        getWord: getWord,
        formatProperty: formatProperty
    };

    function formatPropertyPath(property, value, index) {
        var parsedValue = property.isNumeric ? parseFloat(value) : value;

        return _.set({}, property.path.replace(/<%= index =>/, index), parsedValue);
    }

    function getFormatProperty(property) {
        if (property.path) {
            return formatPropertyPath;
        }

        return additionalFormatFunctions[property.code];
    }

    function formatProperty(property, value, dicts, index) {
        var formatFunc = getFormatProperty(property);
        if (formatFunc) {
            return formatFunc(property, value, index);
        }

        return getWord(property.propName, property.subPropName, value, dicts);
    }

    function getWord(propName, subPropName, value, dicts) {
        var item = _.find(dicts, function(dict) {
            return dict.name === propName;
        });
        if (item) {
            return getItem(item.words, subPropName, value);
        }
    }

    function getItem(list, prop, value) {
        return _.find(list, function(item) {
            return item[prop].toUpperCase() === value.toUpperCase();
        });
    }
}

module.exports = sdImportHelper;
