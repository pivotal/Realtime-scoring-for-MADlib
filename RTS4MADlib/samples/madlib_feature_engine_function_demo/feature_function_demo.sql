/*
Simple sql function to categorize users to demonstrate feature engine sql function
spaladugu@pivotal.io
*/


DO $$
BEGIN

    IF NOT EXISTS(
        SELECT schema_name
          FROM information_schema.schemata
          WHERE schema_name = 'pseo'
      )
    THEN
      EXECUTE 'CREATE SCHEMA pseo';
    END IF;

END
$$;
create or replace function pseo.categorize_users(name text, age int, gender text)
returns setof RECORD as $$
declare 
    rec RECORD;
BEGIN
    select name,  age ,  gender,
        case 
            when age > 50  then 'G4' 
            when age > 40  then 'G3' 
            when age > 30  then 'G3'
            when age > 18  then 'G2'
            when age > 0  then 'G1'
            end as grp into rec;
    return next rec;
end $$ LANGUAGE plpgsql;


create or replace function pseo.categorize_users_feature_driver
() 
returns void as $$
declare 
    uname text;
    uage int;
    ugender text;
begin
    select i.name, i.age, i.gender
    into uname
    , uage, ugender from pseo.message i;

	create table pseo.out_message as
		select *
			from pseo.categorize_users(uname, uage, ugender) as rec(name text, age int, gender text, grp text);
end;
$$ language plpgsql;