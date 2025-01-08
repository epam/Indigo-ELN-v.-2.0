create role crs login password 'crscrscrscrs';

grant usage on schema bingo to crs;
grant select on bingo.bingo_config to crs;
grant select on bingo.bingo_tau_config to crs;

create schema crs authorization crs;

set role crs;

create sequence crs.jobs_id_seq start with 1 increment by 1 ;
create sequence crs.users_id_seq start with 1 increment by 1 ;
create sequence crs.compounds_id_seq start with 1 increment by 1 ;
create sequence crs.compounds_cn_seq start with 1 increment by 1 ;

create table crs.settings(key varchar(4000) primary key, value varchar(4000)) ;
insert into crs.settings(key, value) values ('COMPOUND_NUMBER_PREFIX', 'STR') ;

create table crs.users(
	id integer primary key,
	username varchar(4000) not null,
	password varchar(4000) not null,
	constraint username_unique unique (username)
) ;
-- default credentials: user/pass
insert into crs.users(id, username, password) values (1, 'user', '1a1dc91c907325c69271ddf0c944bc72') ;

create table crs.compounds(
	id integer primary key,
	data text not null,
	userId integer,
	compoundNumber varchar(4000),
	batchNumber varchar(4000),
	casNumber varchar(4000),
	saltCode varchar(4000),
	saltEquivalents numeric,
	comments varchar(4000),
	hazardComments varchar(4000),
	storageComments varchar(4000),
	jobId integer not null,
	conversationalBatchNumber varchar(4000),
	stereoisomerCode varchar(4000),
	constraint compounds_user_id_key foreign key(userId) references crs.users(id)
) ;

--create index bingo_crs_idx on crs.compounds using bingo_idx (data bingo.molecule) ;

create index crc_conversationalbn_idx on crs.compounds (conversationalbatchnumber) ;
create index crc_compoundnumber_idx on crs.compounds (compoundnumber) ;
create index crc_batchnumber_idx on crs.compounds (batchnumber) ;
create index crc_casnumber_idx on crs.compounds (casnumber) ;
create index crc_jobid_idx on crs.compounds (jobid) ;

create function crs.users_id_trigger_bi_proc() returns trigger LANGUAGE plpgsql as
$$
	begin
		new.id := nextval('crs.users_id_seq');
		return new;
	end;
$$;

create trigger users_id_trigger_bi before insert on crs.users for each row
execute procedure crs.users_id_trigger_bi_proc()
;

create function crs.compounds_id_trigger_bi_proc() returns trigger LANGUAGE plpgsql as
$$
	begin
		new.id := nextval('crs.compounds_id_seq');
		return new;
	end;
$$;

create trigger compounds_id_trigger_bi before insert on crs.compounds for each row
execute procedure crs.compounds_id_trigger_bi_proc();
;

create function crs.users_password_trigger_bi_proc() returns trigger LANGUAGE plpgsql as
$$
	begin
		new.password := md5(new.password);
		return new;
	end;
$$;

create trigger users_password_trigger_bi before insert on crs.users for each row
execute procedure crs.users_password_trigger_bi_proc();
;

commit ;
