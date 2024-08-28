drop sequence crs.jobs_id_seq;
drop sequence crs.users_id_seq;
drop sequence crs.compounds_id_seq;
drop sequence crs.compounds_cn_seq;

drop trigger users_id_trigger_bi on crs.users;
drop trigger compounds_id_trigger_bi on crs.compounds;
drop trigger users_password_trigger_bi on crs.users;

drop function crs.users_id_trigger_bi_proc();
drop function crs.compounds_id_trigger_bi_proc();
drop function crs.users_password_trigger_bi_proc();

drop table crs.settings;
drop table crs.compounds;
drop table crs.users;

drop schema crs;

drop owned by crs cascade ;
drop role if exists crs ;

commit;
