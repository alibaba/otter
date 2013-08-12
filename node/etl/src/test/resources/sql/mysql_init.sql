drop table columns;
create table columns 
(	
	id INT(11) AUTO_INCREMENT,
	name VARCHAR(32) ,
	alias_name	char(32) default ' ' not null,
	amount DECIMAL(11,2) , 
	text_b blob,
	text_c TEXT,
	curr_date date not null,
	gmt_create TIMESTAMP not null,
	gmt_modify TIMESTAMP not null,
	CONSTRAINT   columns_pk  PRIMARY   KEY   (id,name) 
) ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

drop retl_client;
create table retl_client
(
	id INT(11) ,
	channel_id int(11),
	CONSTRAINT retl_client_pk PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin ;

drop `erosa`.`offer`;
CREATE TABLE  `erosa`.`offer` (
  `SITE` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `gmt_create` datetime NOT NULL DEFAULT '1999-09-09 00:00:00',
  `gmt_modified` datetime NOT NULL DEFAULT '1999-09-09 00:00:00',
  `gmt_approved` datetime DEFAULT NULL,
  `gmt_post` datetime DEFAULT NULL,
  `ACTION` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `STATUS` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `RECOMMENDED` char(1) COLLATE utf8_bin DEFAULT NULL,
  `category_id_1` bigint(20) DEFAULT NULL,
  `category_id_2` bigint(20) DEFAULT NULL,
  `category_id_3` bigint(20) DEFAULT NULL,
  `category_id_4` bigint(20) DEFAULT NULL,
  `category_id_5` bigint(20) DEFAULT NULL,
  `SUBJECT` varchar(512) COLLATE utf8_bin DEFAULT NULL,
  `PRICE_TERMS` varchar(512) COLLATE utf8_bin DEFAULT NULL,
  `PACKAGING` varchar(512) COLLATE utf8_bin DEFAULT NULL,
  `TYPE` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `gmt_expire` datetime DEFAULT NULL,
  `KEYWORDS` varchar(512) COLLATE utf8_bin DEFAULT NULL,
  `SPECIFICATIONS` varchar(512) COLLATE utf8_bin DEFAULT NULL,
  `QUANTITY` varchar(512) COLLATE utf8_bin DEFAULT NULL,
  `FIRST_NAME` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `COUNTRY` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `LAST_NAME` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `PROVINCE` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `COMPANY` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `CITY` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `EMAIL` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `ADDRESS` varchar(512) COLLATE utf8_bin DEFAULT NULL,
  `ZIP` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `ALT_EMAIL` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `PHONE_COUNTRY` varchar(16) COLLATE utf8_bin DEFAULT NULL,
  `PHONE_AREA` varchar(16) COLLATE utf8_bin DEFAULT NULL,
  `PHONE_NUMBER` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `HOMEPAGE_URL` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `FAX_COUNTRY` varchar(16) COLLATE utf8_bin DEFAULT NULL,
  `FAX_AREA` varchar(16) COLLATE utf8_bin DEFAULT NULL,
  `FAX_NUMBER` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `CSA_ADD` char(1) COLLATE utf8_bin DEFAULT NULL,
  `member_id` varchar(32) COLLATE utf8_bin NOT NULL,
  `WSAD_OFFER` char(1) COLLATE utf8_bin DEFAULT NULL,
  `REMMIT_STATUS` char(1) COLLATE utf8_bin DEFAULT NULL,
  `remmit_date` datetime DEFAULT NULL,
  `remmit_value` decimal(11,2) DEFAULT NULL,
  `REMMIT_WAY` char(1) COLLATE utf8_bin DEFAULT NULL,
  `PICWAY` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  `PICLINK` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `PICSAMPLE` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `TO_CHINESE` char(1) COLLATE utf8_bin DEFAULT NULL,
  `JOIN_FROM` varchar(512) COLLATE utf8_bin DEFAULT NULL,
  `paid_money` bigint(20) DEFAULT NULL,
  `ordering` bigint(20) DEFAULT NULL,
  `SAMPLE_URL` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `SERVICE_VALUE` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `IS_EXPIRED` char(1) COLLATE utf8_bin DEFAULT NULL,
  `company_id` bigint(20) DEFAULT NULL,
  `FEEDBACK_FLAG` char(1) COLLATE utf8_bin DEFAULT NULL,
  `MEMBER_LEVEL` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `MOBILE_PHONE_STATUS` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `SMS_REV_FEEDBACK` char(1) COLLATE utf8_bin DEFAULT NULL,
  `SMS_REV_EXPIRED` char(1) COLLATE utf8_bin DEFAULT NULL,
  `MATURITY` int(11) DEFAULT NULL,
  `PICSAMPLE_ARRAY` varchar(512) COLLATE utf8_bin DEFAULT NULL,
  `MEMBER_DEFINE_PROPERTIES` varchar(6000) COLLATE utf8_bin DEFAULT NULL,
  `OFFER_SPECIAL_SIGN` int(11) DEFAULT NULL,
  `QUALITY_SCORE` int(11) DEFAULT NULL,
  `gmt_client_last_operate` datetime DEFAULT NULL,
  `STATUS_BEFORE_CLIENT_OPERATE` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `REASON` varchar(256) COLLATE utf8_bin DEFAULT NULL,
  `OWNER_ID` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `IS_CHECKED` char(1) COLLATE utf8_bin DEFAULT NULL,
  `TERM_OFFER_PROCESS` int(11) DEFAULT NULL,
  `BRIEF` varchar(2000) COLLATE utf8_bin DEFAULT NULL,
  `price` bigint(20) DEFAULT NULL,
  `quantity_begin` bigint(20) DEFAULT NULL,
  `UNIT` varchar(160) COLLATE utf8_bin DEFAULT NULL,
  `BUYER_LEVEL` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `buy_sub_type` bigint(20) DEFAULT NULL,
  `TRADE_TYPE` int(11) DEFAULT NULL,
  `SIGN` int(11) DEFAULT NULL,
  `batch_no` bigint(20) DEFAULT NULL,
  `gmt_last_repost` datetime DEFAULT NULL,
  `group_id` bigint(20) DEFAULT NULL,
  `column_int1` int(11) DEFAULT NULL,
  `column_int2` int(11) DEFAULT NULL,
  `column_int3` int(11) DEFAULT NULL,
  `column_int4` int(11) DEFAULT NULL,
  `column_varchar1` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `column_varchar2` varchar(64) COLLATE utf8_bin DEFAULT NULL,
  `column_varchar3` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `column_varchar4` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `column_varchar5` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `column_varchar6` varchar(4000) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_offer_mid_st_ge_gid_id_sub` (`member_id`,`STATUS`,`gmt_expire`,`group_id`,`id`,`SUBJECT`(255))
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin


create table retl.retl_buffer 
(	
	id INT(11) AUTO_INCREMENT,
	table_id INT(11) not null,
	type char(1) not null,
	pk_data varchar(256) not null,
	gmt_create TIMESTAMP not null,
	gmt_modified TIMESTAMP not null,
	CONSTRAINT retl_buffer_id PRIMARY KEY (id) 
)  ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin

create table retl.retl_mark
(	
	id INT(11) AUTO_INCREMENT,
	channel_id INT(11) not null,
	CONSTRAINT retl_mark_id PRIMARY KEY (id) 
);

use monitor;
drop table `xdual`;

CREATE TABLE `xdual` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `x` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8