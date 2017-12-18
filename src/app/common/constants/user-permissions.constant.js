var userPermissions = {
    VIEWER: {
        id: 'VIEWER', name: 'VIEWER (read entity)'
    },
    USER: {
        id: 'USER', name: 'USER (read entity, create sub-entities)'
    },
    OWNER: {
        id: 'OWNER', name: 'OWNER (read/update entity, create sub-entities)'
    }
};

module.exports = userPermissions;
