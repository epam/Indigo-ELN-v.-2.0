angular.module('indigoeln')
    .service('DateUtils', function() {
        return {
            convertLocaleDateToServer: function(date) {
                if (date) {
                    var utcDate = new Date();
                    utcDate.setUTCDate(date.getDate());
                    utcDate.setUTCMonth(date.getMonth());
                    utcDate.setUTCFullYear(date.getFullYear());
                    return utcDate;
                } else {
                    return null;
                }
            },
            convertLocaleDateFromServer: function(date) {
                if (date) {
                    var dateString = date.split('-');
                    return new Date(dateString[0], dateString[1] - 1, dateString[2]);
                }
                return null;
            },
            convertDateTimeFromServer: function(date) {
                if (date) {
                    return new Date(date);
                } else {
                    return null;
                }
            }
        };
    });