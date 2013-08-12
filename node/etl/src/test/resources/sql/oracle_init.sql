drop table columns;
create table columns 
(	
	id NUMBER(11)  ,
	name varchar2(32) ,
	alias_name	char(32) default ' ' not null,
	amount number(11,2) , 
	text_b blob,
	text_c clob,
	curr_date date not null,
	gmt_create date not null,
	gmt_modify date not null,
	CONSTRAINT columns_pk  PRIMARY   KEY   (id,name) 
);


drop retl_client;
create table retl_client
(
	id NUMBER(11) ,
	channel_id NUMBER(11),
	CONSTRAINT retl_client_pk PRIMARY KEY (id)
);

drop offer;
CREATE TABLE  OFFER (
  SITE varchar2(64) DEFAULT NULL,
  id number(20) NOT NULL,
  gmt_create date DEFAULT sysdate NOT NULL ,
  gmt_modified date DEFAULT sysdate NOT NULL ,
  gmt_approved date DEFAULT NULL,
  gmt_post date DEFAULT NULL,
  ACTION varchar2(32) DEFAULT NULL,
  STATUS varchar2(32) DEFAULT NULL,
  RECOMMENDED char(1) DEFAULT NULL,
  category_id_1 number(20) DEFAULT NULL,
  category_id_2 number(20) DEFAULT NULL,
  category_id_3 number(20) DEFAULT NULL,
  category_id_4 number(20) DEFAULT NULL,
  category_id_5 number(20) DEFAULT NULL,
  SUBJECT varchar2(512) DEFAULT NULL,
  PRICE_TERMS varchar2(512) DEFAULT NULL,
  PACKAGING varchar2(512) DEFAULT NULL,
  TYPE varchar2(32) DEFAULT NULL,
  gmt_expire date DEFAULT NULL,
  KEYWORDS varchar2(512) DEFAULT NULL,
  SPECIFICATIONS varchar2(512) DEFAULT NULL,
  QUANTITY varchar2(512) DEFAULT NULL,
  FIRST_NAME varchar2(256) DEFAULT NULL,
  COUNTRY varchar2(128) DEFAULT NULL,
  LAST_NAME varchar2(256) DEFAULT NULL,
  PROVINCE varchar2(256) DEFAULT NULL,
  COMPANY varchar2(256) DEFAULT NULL,
  CITY varchar2(256) DEFAULT NULL,
  EMAIL varchar2(256) DEFAULT NULL,
  ADDRESS varchar2(512) DEFAULT NULL,
  ZIP varchar2(64) DEFAULT NULL,
  ALT_EMAIL varchar2(256) DEFAULT NULL,
  PHONE_COUNTRY varchar2(16) DEFAULT NULL,
  PHONE_AREA varchar2(16) DEFAULT NULL,
  PHONE_NUMBER varchar2(256) DEFAULT NULL,
  HOMEPAGE_URL varchar2(256) DEFAULT NULL,
  FAX_COUNTRY varchar2(16) DEFAULT NULL,
  FAX_AREA varchar2(16) DEFAULT NULL,
  FAX_NUMBER varchar2(256) DEFAULT NULL,
  CSA_ADD char(1) DEFAULT NULL,
  member_id varchar2(32) NOT NULL,
  WSAD_OFFER char(1) DEFAULT NULL,
  REMMIT_STATUS char(1) DEFAULT NULL,
  remmit_date date DEFAULT NULL,
  remmit_value number(11,2) DEFAULT NULL,
  REMMIT_WAY char(1) DEFAULT NULL,
  PICWAY varchar2(20) DEFAULT NULL,
  PICLINK varchar2(256) DEFAULT NULL,
  PICSAMPLE varchar2(256) DEFAULT NULL,
  TO_CHINESE char(1) DEFAULT NULL,
  JOIN_FROM varchar2(512) DEFAULT NULL,
  paid_money number(20) DEFAULT NULL,
  ordering number(20) DEFAULT NULL,
  SAMPLE_URL varchar2(256) DEFAULT NULL,
  SERVICE_VALUE varchar2(64) DEFAULT NULL,
  IS_EXPIRED char(1) DEFAULT NULL,
  company_id number(20) DEFAULT NULL,
  FEEDBACK_FLAG char(1) DEFAULT NULL,
  MEMBER_LEVEL varchar2(32) DEFAULT NULL,
  MOBILE_PHONE_STATUS varchar2(32) DEFAULT NULL,
  SMS_REV_FEEDBACK char(1) DEFAULT NULL,
  SMS_REV_EXPIRED char(1) DEFAULT NULL,
  MATURITY number(11) DEFAULT NULL,
  PICSAMPLE_ARRAY varchar2(512) DEFAULT NULL,
  MEMBER_DEFINE_PROPERTIES varchar2(4000) DEFAULT NULL,
  OFFER_SPECIAL_SIGN number(11) DEFAULT NULL,
  QUALITY_SCORE number(11) DEFAULT NULL,
  gmt_client_last_operate date DEFAULT NULL,
  STATUS_BEFORE_CLIENT_OPERATE varchar2(64) DEFAULT NULL,
  REASON varchar2(256) DEFAULT NULL,
  OWNER_ID varchar2(128) DEFAULT NULL,
  IS_CHECKED char(1) DEFAULT NULL,
  TERM_OFFER_PROCESS number(11) DEFAULT NULL,
  BRIEF varchar2(2000) DEFAULT NULL,
  price number(20) DEFAULT NULL,
  quantity_begin number(20) DEFAULT NULL,
  UNIT varchar2(160) DEFAULT NULL,
  BUYER_LEVEL varchar2(64) DEFAULT NULL,
  buy_sub_type number(20) DEFAULT NULL,
  TRADE_TYPE number(11) DEFAULT NULL,
  SIGN number(11) DEFAULT NULL,
  batch_no number(20) DEFAULT NULL,
  gmt_last_repost date DEFAULT NULL,
  group_id number(20) DEFAULT NULL,
  column_int1 number(11) DEFAULT NULL,
  column_int2 number(11) DEFAULT NULL,
  column_int3 number(11) DEFAULT NULL,
  column_int4  number(11) DEFAULT NULL,
  column_varchar1 varchar2(32) DEFAULT NULL,
  column_varchar2 varchar2(64) DEFAULT NULL,
  column_varchar3 varchar2(128) DEFAULT NULL,
  column_varchar4 varchar2(128) DEFAULT NULL,
  column_varchar5 varchar2(128) DEFAULT NULL,
  column_varchar6 varchar2(4000) DEFAULT NULL,
  CONSTRAINT offer_pk PRIMARY KEY (id)
);

CREATE INDEX idx_offer_mid_st_ge_gid_id_sub on OFFER(member_id,STATUS,gmt_expire,group_id,id,SUBJECT)

create table retl.retl_buffer 
(	
	id number(11) not null,
	table_id number(11) not null,
	type char(1) not null,
	pk_data varchar(256) not null,
	gmt_create DATE not null,
	gmt_modified DATE not null,
	CONSTRAINT retl_buffer_id PRIMARY KEY (id) 
);

create table retl.retl_mark
(	
	id number(11) not null,
	channel_id number(11) not null,
	CONSTRAINT retl_mark_id PRIMARY KEY (id) 
);