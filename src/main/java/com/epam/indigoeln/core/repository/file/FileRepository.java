package com.epam.indigoeln.core.repository.file;

import com.epam.indigoeln.core.model.User;
import com.epam.indigoeln.core.repository.util.ConverterUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.where;

@Repository
public class FileRepository {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    public GridFSFile store(InputStream content, String filename, String contentType, User author) {
        return gridFsTemplate.store(content, filename, contentType, getMetadata(author));
    }

    public GridFSDBFile findOneById(String id) {
        return gridFsTemplate.findOne(query(where("_id").is(id)));
    }

    public Page<GridFSDBFile> findAll(List<String> ids, Pageable pageable) {
        List<GridFSDBFile> list = gridFsTemplate.find(query(where("_id").in(ids))
                .with(pageable));
        return new PageImpl<>(list, pageable, ids.size());
    }

    public void delete(String id) {
        gridFsTemplate.delete(query(where("_id").is(id)));
    }

    private DBObject getMetadata(User author) {
        DBObject metadata = new BasicDBObject(1);
        // Adding metadata about "author"
        ConverterUtil.setAuthorToFileMetadata(metadata, author);

        return metadata;
    }
}