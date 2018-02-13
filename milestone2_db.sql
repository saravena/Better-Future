-- THIS FILE IS JUST TO KEEP TRACK OF DATABASE THINGS THAT GET CREATED DURING MILESTONE 2
-- we'll put everything together later

-- returning all the information for a single administrator at login
create or replace procedure check_login_admin (in user_login varchar2(10),
						out password varchar2(10), out name varchar2(20),
						out email varchar2(30), out address varchar2(30))
	begin
		select c.password into password, c.name into name,
		c.email into email, c.address into address
		from ADMINISTRATOR c
		where login = user_login;
	end;
/

-- returns the symbol of the mutual funds this customer prefers to invest in
-- and the percentage of the user's investment that should be invested in this mutual fund
create or replace procedure specific_customer_preferences (in user_login varchar2(10))
	begin
		select symbol, percentage
		from customer_prefrences
		where login = user_login;
	end;
/

create or replace trigger increment_transID 
	before insert on TRXLOG
	for each row
	begin
		select max(trans_id)+1 into :new.trans_id
		from TRXLOG;
	end;
/

create or replace trigger increment_allocNo
	before insert on ALLOCATION
	for each row
	begin
		select max(trans_id) + 1 into :new.allocation_no
		from ALLOCATION;
	end;
/

-- trigger to increase the customer balance when a 'sell' action is inserted into the trxlog table
create or replace trigger increase_customer_balance
	before insert on TRXLOG
	for each row
	when (new.action = 'sell')
	begin
		update CUSTOMER
		set balance = balance + :new.amount;
		where login = :new.login;

		update OWNS
		set shares = shares - :new.num_shares;
		where login = :new.login and symbol = :new.symbol;
	end;
/

-- trigger to decrease the customer balance when a 'buy' action is inserted into the trxlog table
create or replace trigger decrease_customer_balance
	before insert on TRXLOG
	for each row
	when (new.action = 'buy')
	begin
		select balance 
		from CUSTOMER
		where login = :new.login;

		-- the customer has enough money to buy this amount of shares 
		if balance >= :new.amount
		then
			update CUSTOMER
			set balance = balance - :new.amount
			where login = :new.login;
		end if;

		-- incrementing the number of share the customer owns with the new amount 
		select count(*)
		from OWNS
		where login = :new.login and symbol = :new.symbol;

		if count(*) = 0
		then
			insert into OWNS values(:new.login, :new.symbol, :new.num_shares);
		else
			select shares
			from OWNS
			where login = :new.login and symbol = :new.symbol;

			update OWNS
			set shares = shares + :new.num_shares
			where login = :new.login and symbol = :new.symbol;
		end if;
	end;
/

create or replace view alloc_pref
	select *
	from ALLOCATION natural join PREFERS;

-- Trigger set to insert buy transcations following the deposit insert into log
-- when a deposit is made, it is assumed that the deposit money is going towards
-- a buy to the customers mutual funds (the ones that preferenced)
create or replace trigger on_deposit_log
	after insert on TRXLOG
	for each row
	when (new.action = 'deposit')
	begin
		declare amount_to_invest float;
		declare num_shares int;
		declare single_price float;

		declare cursor user_pref is
			select *
			from alloc_pref
			where login = :new.login;
		declare pref_record alloc_pref%rowtype;

		-- for every tuple in the cursor user_pref
		loop

			fetch user_pref into pref_record
			exit when user_pref%NOTFOUND;

			amount_to_invest = :new.amount * pref_record.percentage;

			-- call procedure num_shares_from_input_price(symbol, amount_to_invest, int num_shares, int share_price)
			select round(amount_to_invest/price, 0, 1) into num_shares, price into single_price
			from MUTUALFUND natural join CLOSINGPRICE
			where symbol = :new.symbol; 
			-- insert into TRXLOG values(trans_id++, login, symbol, date, 'buy', num_shares, share_price, amount_to_invest);
			insert into TRXLOG values(1, :new.login, :new.symbol, :new.t_date, 'buy', num_shares, single_price, amount_to_invest);
				
		end loop;

		close user_pref;
	end;
/
