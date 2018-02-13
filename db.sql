
drop table MUTUALFUND cascade constraints;
drop table CLOSINGPRICE cascade constraints;
drop table CUSTOMER cascade constraints;
drop table ADMINISTRATOR cascade constraints;
drop table ALLOCATION cascade constraints;
drop table PREFERS cascade constraints;
drop table TRXLOG cascade constraints;
drop table OWNS cascade constraints;
drop table MUTUALDATE cascade constraints;

purge recyclebin;

-- Table listing the information of a variety of mutual funds, categorized appropriately
create table MUTUALFUND (
	symbol varchar2(20),
	name varchar2(40),
	description varchar2(100),
	category varchar2(10),
	c_date date,
	constraint mutfund_name check (name in ('money-market', 'real-estate') and category = 'fixed'
					or name in ('short-term-bonds', 'long-term-bonds') and category = 'bonds'
					or name in ('balance-bonds-stocks', 'social-responsibility-bonds-stocks') and category = 'mixed'
					or name in ('general-stocks', 'aggressive-stocks', 'international-markets-stocks') and category = 'stocks'),
	constraint pk_mutualfund primary key(symbol) deferrable initially immediate
);

-- Tracks the closing prices of the mutual funds
create table CLOSINGPRICE (
	symbol varchar2(20),
	price float(2),
	p_date date,
	constraint pk_closingprice primary key(symbol, p_date) deferrable initially immediate,
	constraint fk_closingprice_mutualfund foreign key(symbol)
		references MUTUALFUND(symbol) deferrable initially immediate
);

-- Stores unique customer information
create table CUSTOMER (
	login varchar2(10),
	name varchar2(20),
	email varchar2(30),
	address varchar2(30),
	password varchar2(10),
	balance float(2),
	constraint pk_customer primary key(login) deferrable initially immediate
);

-- Stores unique administrator information
create table ADMINISTRATOR (
	login varchar2(10),
	name varchar2(20),
	email varchar2(30),
	address varchar2(30),
	password varchar2(10),
	constraint pk_administrator primary key(login) deferrable initially immediate
);

-- Holds allocation information
create table ALLOCATION (
	allocation_no int,
	login varchar2(10),
	p_date date,
	constraint pk_allocation primary key(allocation_no) deferrable initially immediate,
	constraint fk_allocation_customer foreign key(login)
		references CUSTOMER(login) deferrable initially immediate
);

-- Holds the current preferences of a user
-- Update: there will be a check in Milestone 2 making sure that it has been 30 days
-- (or a month) to update the preferenes, but also so that the preference changes
-- with the distributions equal 100%
create table PREFERS (
	allocation_no int,
	symbol varchar2(20),
	percentage float(2),
	constraint pk_prefers primary key(allocation_no, symbol) deferrable initially immediate,
	constraint fk_prefers_alloc foreign key(allocation_no)
		references ALLOCATION(allocation_no) deferrable initially immediate,
	constraint fk_prefers_mutfund foreign key(symbol)
		references MUTUALFUND(symbol) deferrable initially immediate
);

-- log used to track every transaction done under an account
create table TRXLOG (
	trans_id int,
	login varchar2(10),
	symbol varchar2(20),
	t_date date,
	action varchar2(10),
	num_shares int,
	price float(2),
	amount float(2),
	-- Makes a check to make sure the action is one of the three listed: deposit, sell, buy
	constraint trx_action check (action in ('deposit', 'sell', 'buy')), 
	constraint pk_trxlog primary key(trans_id) deferrable initially immediate,
	constraint fk_trxlog_cust foreign key(login)
		references CUSTOMER(login) deferrable initially immediate,
	constraint fk_trxlog_mutfund foreign key(symbol)
		references MUTUALFUND(symbol) deferrable initially immediate
);

-- Information of the mutual funds owned by a user
create table OWNS(
	login varchar2(10),
	symbol varchar(20),
	shares int,
	constraint pk_owns primary key(login, symbol) deferrable initially immediate,
	constraint fk_own_cust foreign key(login)
		references CUSTOMER(login) deferrable initially immediate,
	constraint fk_own_mutfund foreign key(symbol)
		references MUTUALFUND(symbol) deferrable initially immediate
);

-- Holds the date
create table MUTUALDATE (
	c_date date,
	constraint pk_mutdate primary key(c_date) deferrable initially immediate
);

commit;
