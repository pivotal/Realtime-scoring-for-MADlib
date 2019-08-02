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

create or replace function pseo.categorize_user(age int)
returns text as $$
  declare grp text;
BEGIN
	select 
		case 
			when age > 50  then 'G4' 
			when age > 40  then 'G3' 
			when age > 30  then 'G3'
			when age > 18  then 'G2'
			when age > 0  then 'G1'
	end into grp;
	return grp;
end $$ LANGUAGE plpgsql;