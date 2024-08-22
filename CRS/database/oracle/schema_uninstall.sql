drop sequence crs.jobs_id_seq ;
drop sequence crs.users_id_seq ;
drop sequence crs.compounds_id_seq ;
drop sequence crs.compounds_cn_seq ;

drop table crs.settings ;
drop table crs.compounds ;
drop table crs.users ;

drop function crs.md5 ;

drop tablespace crs;
drop tablespace crs_temp;

drop user crs cascade;

