var roles = require('./permission-roles.json');

var permissionsConstant = {
    PROJECT_OWNER_AUTHORITY_SET: [roles.PROJECT_READER, roles.PROJECT_CREATOR, roles.NOTEBOOK_READER, roles.NOTEBOOK_CREATOR],
    PROJECT_USER_AUTHORITY_SET: [roles.PROJECT_READER, roles.NOTEBOOK_READER, roles.NOTEBOOK_CREATOR],
    PROJECT_VIEWER_AUTHORITY_SET: [roles.PROJECT_READER],
    NOTEBOOK_OWNER_AUTHORITY_SET: [roles.NOTEBOOK_READER, roles.NOTEBOOK_CREATOR, roles.EXPERIMENT_READER, roles.EXPERIMENT_CREATOR],
    NOTEBOOK_USER_AUTHORITY_SET: [roles.NOTEBOOK_READER, roles.EXPERIMENT_READER, roles.EXPERIMENT_CREATOR],
    NOTEBOOK_VIEWER_AUTHORITY_SET: [roles.NOTEBOOK_READER],
    EXPERIMENT_OWNER_AUTHORITY_SET: [roles.EXPERIMENT_READER, roles.EXPERIMENT_CREATOR],
    EXPERIMENT_VIEWER_AUTHORITY_SET: [roles.EXPERIMENT_READER],

    removeProjectWarning: 'You are trying to remove USER who has access to notebooks or ' +
    'experiments within this project. By removing this USER you block his (her) ' +
    'access to notebook or experiments withing this project',
    removeNotebookWarning: 'You are trying to remove USER who has access to experiments within this notebook. ' +
    'By removing this USER you block his (her) access to experiments withing this notebook',
    checkAuthorityWarning: function(permission) {
        return 'This user cannot be set as ' + permission + ' as he does not have ' +
            'sufficient privileges in the system, please select another permissions level';
    }
};

module.exports = permissionsConstant;
