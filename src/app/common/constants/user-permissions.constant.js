var userPermissions = {
    VIEWER: {
        id: 'VIEWER', name: 'VIEWER (read entity)'
    },
    USER: {
        id: 'USER', name: 'USER (read entity, create experiments)'
    },
    OWNER: {
        id: 'OWNER', name: 'OWNER (read/update entity, create experiments)'
    }
};

module.exports = userPermissions;
