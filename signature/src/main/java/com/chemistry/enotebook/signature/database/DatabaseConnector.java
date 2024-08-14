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
package com.chemistry.enotebook.signature.database;

import com.chemistry.enotebook.signature.IndigoSignatureServiceException;
import com.chemistry.enotebook.signature.Util;
import com.chemistry.enotebook.signature.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

public class DatabaseConnector {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnector.class);

    private DataSource dataSource;
    private String driverClassName;

    @Required
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static String TEMPLATE_TABLE = "SignatureTemplate";
    private static String TEMPLATE_SIGNATURE_BLOCK_TABLE = "TemplateSignatureBlock";
    private static String USER_TABLE = "UserAccount";
    private static String DOCUMENT_TABLE = "Document";
    private static String DOCUMENT_SIGNATURE_BLOCK_TABLE = "DocumentSignatureBlock";

    private static String selectAllTemplatesSQL = "select tp.templateid, tp.templatename, tp.userid as templateAuthorId, sb.templatesignatureblockid, sb.userid, sb.reasonid, sb.templatesignatureblockindex from " + TEMPLATE_TABLE + " tp, " + TEMPLATE_SIGNATURE_BLOCK_TABLE + " sb, " + USER_TABLE + " ur where sb.templateid=tp.templateid and tp.userid=ur.userid ";
    private static String selectSingleTemplateByIdSQL = selectAllTemplatesSQL + " and tp.templateid=?";
    private static String selectAllUsersTemplatesSQL = selectAllTemplatesSQL + " and ur.username=?";

    private static String createDocumentSQL = "insert into " + DOCUMENT_TABLE + " (documentid, name, status, creationdate, lastupdatedate, userid, documentfile) values (?,?,?,?,?,?,?)";
    private static String createDocumentSignatureBlockSQL = "insert into " + DOCUMENT_SIGNATURE_BLOCK_TABLE + " (documentsignatureblockid, documentid, documentsignatureblockindex, userid, reasonid, status, inspected) values (?,?,?,?,?,?,?)";
    private static String updateDocumentPropertiesSQL = "update " + DOCUMENT_TABLE + " set name=?, status=?, lastupdatedate=? where documentid=?";
    private static String updateDocumentContentSQL = "update " + DOCUMENT_TABLE + " set documentfile=? where documentid=?";
    private static String updateDocumentSignatureBlockSQL = "update " + DOCUMENT_SIGNATURE_BLOCK_TABLE + " set documentsignatureblockindex=?, userid=?, reasonid=?, status=?, actiondate=?, comments=?, inspected=? where documentsignatureblockid=?";
    private static String removeDocumentSQL = "delete from " + DOCUMENT_TABLE + " where documentid=?";

    private static String selectUserSQL = "select userid, username, firstname, lastname, email from " + USER_TABLE;
    private static String selectAllDocumentsSQL =
            "select dt.documentid, dt.name, dt.status as documentstatus, dt.creationdate, dt.lastupdatedate, author.userid as authorid, author.username as authorusername, author.firstname as authorfirstname, author.lastname as authorlastname, author.email as authoremail, " +
                   "ds.documentsignatureblockid, ds.documentsignatureblockindex, ds.reasonid, ds.actiondate, ds.status as signerStatus, ds.comments as \"comment\", ds.inspected, signer.userid as signerid, signer.username as signerusername, signer.firstname as  signerfirstname, signer.lastname as signerlastname, signer.email as signeremail\n" +
            "from " + DOCUMENT_TABLE + " dt, " + DOCUMENT_SIGNATURE_BLOCK_TABLE + " ds, (" + selectUserSQL + ") author, (" + selectUserSQL + ") signer " +
            "where dt.documentid=ds.documentid and author.userid=dt.userid and signer.userid=ds.userid";
    private static String selectOnlyDocumentsIdsForUserSQL =
            "select distinct dt.documentid as userDocumentId from " + DOCUMENT_TABLE + " dt, " + DOCUMENT_SIGNATURE_BLOCK_TABLE + " ds, (" + selectUserSQL + ") author, (" + selectUserSQL + ") signer " +
                    "where dt.documentid=ds.documentid and author.userid=dt.userid and signer.userid=ds.userid and (author.username=? or signer.username=?)";
    private static String selectWholeUsersDocuments =
            "select  dt.documentid, dt.name, dt.status as documentstatus, dt.creationdate, dt.lastupdatedate," +
                    "ds.documentsignatureblockid, ds.documentsignatureblockindex, ds.reasonid, ds.actiondate, ds.status as signerStatus, ds.comments as \"comment\", ds.inspected," +
                    "author.userid as authorid, author.username as authorusername, author.firstname as authorfirstname, author.lastname as authorlastname,\n" +
                    "signer.userid as signerid, signer.username as signerusername, signer.firstname as  signerfirstname, signer.lastname as signerlastname, signer.email as signeremail\n" +
                    "from " + DOCUMENT_TABLE + " dt, " + DOCUMENT_SIGNATURE_BLOCK_TABLE + " ds, (" + selectUserSQL + ") author, (" + selectUserSQL + ") signer,\n" +
                    "(" + selectOnlyDocumentsIdsForUserSQL + ") userDocumentIds\n" +
                    "where dt.documentid=ds.documentid and author.userid=dt.userid and signer.userid=ds.userid and dt.documentId=userDocumentIds.userDocumentId";

    private static String selectSingleDocumentSQL = selectAllDocumentsSQL + " and dt.documentid=?";
    private static String selectAllUsersDocumentsSQL = selectAllDocumentsSQL + " and (author.username=? or signer.username=?)";


    private static String selectDocumentContentSQL = "select dt.documentfile from " + DOCUMENT_TABLE + " dt where dt.documentid=?";

    private static String createUserSQL = "insert into " + USER_TABLE + " (userid, active, admin, creationdate, createdby, username, password, firstname, lastname, email) values (?,?,?,?,?,?,?,?,?,?)";
    private static String updateUserSQL = "update " + USER_TABLE + " set active=?, administrator=?, username=?, password=?, firstname=?, lastname=?, email=? where userid=?";
    private static String getAllStatuses = "select * from Status";
    private static String getAllReasons = "select * from Reason";

    private static final List<String> uuidColumns = Arrays.asList("id", "userid", "authorid", "signerid", "templateid", "templateauthorid", "documentid", "documentsignatureblockid", "templatesignatureblockid");
    private static final List<String> booleanColumns = Arrays.asList("active", "admin", "inspected");

    public void addDocument(Document document) {
        execute(createDocumentSQL, document.getId(), document.getDocumentName(), document.getStatus().code(), Util.convertToSqlDate(document.getCreationDate()), Util.convertToSqlDate(document.getLastUpdateDate()), document.getAuthor().getUserId(), document.getContent());
        for(DocumentSignatureBlock documentSignatureBlock : document.getDocumentSignatureBlocks()) {
            execute(createDocumentSignatureBlockSQL , documentSignatureBlock.getId(), document.getId(), documentSignatureBlock.getIndex(), documentSignatureBlock.getSigner().getUserId(), documentSignatureBlock.getReason().id(), documentSignatureBlock.getStatus().code(), documentSignatureBlock.isInspected());
        }
    }

    public Document getDocument(String documentId) {
        Set<Map> documentAndSigningStatuses = execute(selectSingleDocumentSQL, UUID.fromString(documentId));
        Collection<Document> document = extractDocuments(documentAndSigningStatuses);

        if(document.iterator().hasNext())
            return document.iterator().next();
        return null;
    }
    public byte[] getDocumentContent(String documentId) {
        return getDocumentContent(UUID.fromString(documentId));
    }

    public byte[] getDocumentContent(UUID id) {
        Set<Map> documentContent = execute(selectDocumentContentSQL, id);

        if(documentContent.size() > 0) {
            return (byte[])documentContent.iterator().next().get("documentfile");
        }
        return null;
    }

    public Collection<Document> getDocuments(String username) {
        if(username!=null) {
            Set<Map> documentsAndSigningStatuses = execute(selectWholeUsersDocuments, username, username);
            return extractDocuments(documentsAndSigningStatuses);
        }
        return null;
    }

    public void updateDocument(Document document, boolean updateContent) {
        execute(updateDocumentPropertiesSQL, document.getDocumentName(), document.getStatus().code(), Util.convertToSqlDate(document.getLastUpdateDate()), document.getId());

        if(updateContent) {
            execute(updateDocumentContentSQL, document.getContent(), document.getId());
        }

        for(DocumentSignatureBlock documentSignatureBlock : document.getDocumentSignatureBlocks()) {
            execute(updateDocumentSignatureBlockSQL, documentSignatureBlock.getIndex(), documentSignatureBlock.getSigner().getUserId(), documentSignatureBlock.getReason().id(), documentSignatureBlock.getStatus().code(), Util.convertToSqlDate(documentSignatureBlock.getActionDate()), documentSignatureBlock.getComment(), documentSignatureBlock.isInspected(), documentSignatureBlock.getId());
        }
    }

    public void removeDocument(UUID id) {
        execute(removeDocumentSQL, id);
    }

    public Template getTemplate(UUID id) {
        Set<Map> result = execute(selectSingleTemplateByIdSQL, id);

        Collection<Template> template =  extractTemplates(result);

        if(template != null  && template.iterator().hasNext())
            return template.iterator().next();

        return null;
    }

    public void removeTemplate(UUID id) {
        execute("delete from " + TEMPLATE_TABLE + " where templateid=?", id);
    }

    public User getUserByUsername(String username) {
        Set<Map> result = execute("select * from " + USER_TABLE + " where username=?", username);

        if (result.iterator().hasNext())
            return User.generateUser(result.iterator().next());

        return null;
    }

    public User getUserByUserId(String userId) {
        Set<Map> result = execute("select * from " + USER_TABLE + " where userid=?", UUID.fromString(userId));

        if(result.iterator().hasNext())
            return User.generateUser(result.iterator().next());
        return null;
    }

    public Collection<User> getUsersByPartOfName(String partOfName, int limit) {
        String sql = "select * from " + USER_TABLE + " where (LOWER(username) like ? or LOWER(firstname) like ? or LOWER(lastname) like ?)";

        if(limit > 0) {
            sql = DatabaseDifferences.getQueryWithLimit(driverClassName, sql, limit);
        }

        partOfName = "%" + partOfName.toLowerCase() + "%";

        Set<Map> result = execute(sql, partOfName, partOfName, partOfName);
        Set<User> users = new HashSet<User>();
        for(Map row : result) {
            users.add(User.generateUser(row));
        }
        return users;
    }

    public void addTemplate(Template template) {
        execute("INSERT INTO " + TEMPLATE_TABLE + " (templateId, templateName, userid) VALUES (?, ?, ?)", template.getId(), template.getName(), template.getAuthor().getUserId());

        for(TemplateSignatureBlock templateSignatureBlock : template.getTemplateSignatureBlocks()) {
            execute("INSERT INTO " + TEMPLATE_SIGNATURE_BLOCK_TABLE + " (" +
                    "templatesignatureblockid, userId, reasonId, templatesignatureblockindex, templateId) " +
                    "VALUES (?, ?, ?, ?, ?)",
                    java.util.UUID.randomUUID(), templateSignatureBlock.getUser().getUserId(), templateSignatureBlock.getReason().id(),
                    templateSignatureBlock.getIndex(), template.getId());
        }
    }

    public Collection<Template> getTemplates(String username) {
        Set<Map> templatesAndSignatureTemplates = execute(selectAllUsersTemplatesSQL, username);
        return extractTemplates(templatesAndSignatureTemplates);
    }

    public Collection<Document> extractDocuments(Set<Map> documentsAndSigningStatuses) {
        Map<UUID, Document> documents = new HashMap<UUID, Document>();

        for(Map row : documentsAndSigningStatuses) {
            UUID documentId = (UUID)row.get("documentid");

            Document document = documents.get(documentId);
            if(document == null) {
                document = Document.generateDocument(row);
                documents.put(document.getId(), document);
            }
            document.addDocumentSignatureBlock(DocumentSignatureBlock.generateDocumentSigningStatus(row));
        }

        return documents.values();
    }

    public Collection<Template> extractTemplates(Set<Map> templatesAndSignatureTemplates) {
        Map<UUID, Template> templates = new HashMap<UUID, Template>();

        for(Map row : templatesAndSignatureTemplates) {
            UUID templateId = (UUID)row.get("templateid");

            Template template = templates.get(templateId);
            if(template == null) {
                template = Template.generateTemplate(row, this);
                templates.put(template.getId(), template);
            }
            template.addSignatureBlock(TemplateSignatureBlock.generateTemplate(row, this));
        }

        return templates.values();
    }

    public void createUser(User user) {
        execute(createUserSQL, user.getUserId(), user.isActive(), user.isAdmin(), Util.convertToSqlDate(user.getCreationDate()), user.getCreatedBy(), user.getUsername(), user.getPassword(), user.getFirstName(), user.getLastName(), user.getEmail());
    }
    public void updateUser(User user) {
        StringBuilder sb = new StringBuilder();
        boolean first=false;
        List parameters = new ArrayList();

        sb.append("update ").append(DatabaseConnector.USER_TABLE).append(" set ");

        first = appendProperty(sb, User.USERNAME, user.getUsername(), parameters, first) || first;
        first = appendProperty(sb, User.FIRST_NAME, user.getFirstName(), parameters, first) || first;
        first = appendProperty(sb, User.LAST_NAME, user.getLastName(), parameters, first) || first;
        first = appendProperty(sb, User.EMAIL, user.getEmail(), parameters, first) || first;
        first = appendProperty(sb, User.ADMIN, user.isAdmin(), parameters, first) || first;
        first = appendProperty(sb, User.ACTIVE, user.isActive(), parameters, first) || first;
        first = appendProperty(sb, User.PASSWORD, user.getPassword(), parameters, first) || first;

        if(first) {
            sb.append("where userid=?");
            parameters.add(user.getUserId());
            execute(sb.toString(), parameters.toArray());
        }
    }

    private Set<Map> execute(String sql, Object...parameters) {
        Connection con = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            prepStmt = con.prepareStatement(sql);

            for (int i = 0; i < parameters.length; i++) {
                Object obj = parameters[i];
                if (obj instanceof UUID || obj instanceof Boolean) {
                    obj = obj.toString();
                }
                prepStmt.setObject(i + 1, obj);
            }

            if(sql.toLowerCase().contains("select")) {
                rs = prepStmt.executeQuery();
                return resultSetToHashSet(rs);
            } else {
                prepStmt.executeUpdate();
                return null;
            }
        } catch (Exception e) {
            log.warn("Error executing SQL query: " + e.getMessage());
            throw new IndigoSignatureServiceException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (prepStmt != null) {
                    prepStmt.close();
                }
            } catch (Exception ignored) {
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public Set<Map> resultSetToHashSet(ResultSet rs) throws SQLException{
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        Set<Map> table = new HashSet<Map>();

        while (rs.next()){
            HashMap row = new HashMap();
            for(int i=1; i<=columns; i++){
                String columnName = md.getColumnName(i);
                Object columnData = rs.getObject(i);

                if (columnName != null) {
                    // In PostgreSQL - sql type UUID, in Oracle - sql type varchar
                    if (uuidColumns.contains(columnName.toLowerCase())) {
                        if (columnData instanceof String) {
                            columnData = UUID.fromString((String) columnData);
                        }
                    }
                    // In PostgreSQL - sql type BOOLEAN, in Oracle - CHAR
                    if (booleanColumns.contains(columnName.toLowerCase())) {
                        if (columnData instanceof String) {
                            columnData = Boolean.valueOf((String) columnData);
                        }
                    }
                    // In PostgreSQL - SMALLINT maps to Integer, in Oracle - to BigDecimal
                    if (columnData instanceof Number) {
                        columnData = ((Number) columnData).intValue();
                    }
                    // oracle.sql.CLOB - to String
                    if (columnData instanceof Clob) {
                        columnData = ((Clob) columnData).getSubString(1, (int) ((Clob) columnData).length());
                    }
                    // oracle.sql.BLOB - to byte[]
                    if (columnData instanceof Blob) {
                        columnData = ((Blob) columnData).getBytes(1, (int) ((Blob) columnData).length());
                    }
                    // Oracle TIMESTAMP - to Java Timestamp
                    if (columnData != null && columnData.getClass().getName().contains("oracle.sql.TIMESTAMP")) {
                        try {
                            Method method = columnData.getClass().getMethod("timestampValue");
                            columnData = method.invoke(columnData);
                        } catch (Exception e) {
                            columnData = null;
                        }
                    }
                    // In Oracle - column names in UPPER CASE, we should use lower case
                    columnName = columnName.toLowerCase();
                }

                row.put(columnName, columnData);
            }
            table.add(row);
        }
        return table;
    }

    private boolean appendProperty(StringBuilder sb, String propertyName, Object propertyValue, List parameters, boolean previousFieldExists) {
        if(propertyValue != null) {
            if(previousFieldExists) {
                sb.append(",");
            }
            sb.append(propertyName).append("=? ");
            parameters.add(propertyValue);
            return true;
        }
        return false;
    }
    
    public void queueForFtp(UUID documentId) {
    	String sql = "INSERT INTO FtpQueue(Id,DocumentId,FtpStatus) VALUES(?, ?, ?)"; 
    	
    	execute(sql, UUID.randomUUID(), documentId, FtpQueueStatus.QUEUED.name());
    }
    
    public void updateFtpQueue(UUID documentId, FtpQueueStatus status) {
    	String sql = "UPDATE FtpQueue SET FtpStatus = ? where DocumentId = ?";
    	
    	execute(sql, status.name(), documentId);
    }
    
    public UUID dequeueForFtp() {
    	String sql = "SELECT DocumentId FROM FtpQueue WHERE FtpStatus = ? ORDER BY Id ";

        sql = DatabaseDifferences.getQueryWithLimit(driverClassName, sql, 1);

    	Set<Map> result = execute(sql, FtpQueueStatus.QUEUED.name());
    	if ((result == null) || result.isEmpty()) {
    		return null;
    	}
    	// just take the first value;
    	Map m = result.iterator().next();
    	if ((m == null) || m.isEmpty()) {
    		return null;
    	}
    	return (UUID)m.get("documentid");
    }

    public Set<Map> getAllStatuses() {
        return execute(getAllStatuses);
    }

    public Set<Map> getAllReasons() {
        return execute(getAllReasons);
    }
}
