-- drop user and schema

DROP OWNED BY signature_service CASCADE ;
DROP ROLE IF EXISTS signature_service ;

-- commit changes

COMMIT ;
