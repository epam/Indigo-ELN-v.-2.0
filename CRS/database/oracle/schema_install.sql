create tablespace crs datafile 'crs.ora' size 5M reuse autoextend on next 5M maxsize unlimited extent management local;
create temporary tablespace crs_TEMP tempfile 'crs_TEMP.ora' size 5M reuse autoextend on next 5M maxsize unlimited extent management local;
create user crs identified by crscrscrscrs default tablespace crs temporary tablespace crs_TEMP;
grant connect, resource to crs;
grant execute on dbms_crypto to crs;

create sequence crs.jobs_id_seq start with 1 increment by 1 nomaxvalue ;
create sequence crs.users_id_seq start with 1 increment by 1 nomaxvalue ;
create sequence crs.compounds_id_seq start with 1 increment by 1 nomaxvalue ;
create sequence crs.compounds_cn_seq start with 1 increment by 1 nomaxvalue ;

create table crs.settings(key varchar2(4000) primary key, value varchar2(4000)) ;
insert into crs.settings(key, value) values ('COMPOUND_NUMBER_PREFIX', 'STR') ;

create table crs.users(
    id integer primary key,
    username varchar2(4000) not null,
    password varchar2(4000) not null,
    constraint username_unique unique (username)) ;

create table crs.compounds(
    id integer primary key,
    data clob not null,
    userId integer,
    compoundNumber varchar2(4000),
    batchNumber varchar2(4000),
    casNumber varchar2(4000),
    saltCode varchar2(4000),
    saltEquivalents binary_double,
    comments varchar2(4000),
    hazardComments varchar2(4000),
    storageComments varchar2(4000),
    jobId integer not null,
    conversationalBatchNumber varchar2(4000),
    stereoisomerCode varchar2(4000),
    constraint compounds_user_id_key foreign key(userId) references crs.users(id)) ;

--create index crs.bingo_crs_idx on crs.compounds(data) indextype is bingo.MoleculeIndex ;

create index crs.crc_conversationalbn_idx on crs.compounds (conversationalbatchnumber) ;
create index crs.crc_compoundnumber_idx on crs.compounds (compoundnumber) ;
create index crs.crc_batchnumber_idx on crs.compounds (batchnumber) ;
create index crs.crc_casnumber_idx on crs.compounds (casnumber) ;
create index crs.crc_jobid_idx on crs.compounds (jobid) ;

create or replace function crs.md5 (input VARCHAR2) return VARCHAR2 is
begin
    return lower(rawtohex(dbms_crypto.hash(utl_raw.cast_to_raw(input), dbms_crypto.HASH_MD5)));
end;
/

create or replace trigger crs.users_id_trigger_bi before insert on crs.users for each row
begin
    select crs.users_id_seq.nextval into :new.id from dual;
end;
/

create or replace trigger crs.compounds_id_trigger_bi before insert on crs.compounds for each row
begin
    select crs.compounds_id_seq.nextval into :new.id from dual;
end;
/

create or replace trigger crs.users_password_trigger_bi before insert on crs.users for each row
begin
    select crs.md5(:new.password) into :new.password from dual;
end;
/

commit ;
