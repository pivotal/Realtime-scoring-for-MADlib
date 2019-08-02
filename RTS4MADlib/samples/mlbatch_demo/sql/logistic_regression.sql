DO $$
BEGIN

    IF NOT EXISTS(
        SELECT schema_name
          FROM information_schema.schemata
          WHERE schema_name = 'madlib_demo'
      )
    THEN
      EXECUTE 'CREATE SCHEMA madlib_demo';
    END IF;

END
$$;


DROP TABLE IF EXISTS madlib_demo.patients;

CREATE TABLE madlib_demo.patients( id INTEGER NOT NULL,
                       second_attack INTEGER,
                       treatment INTEGER,
                       trait_anxiety INTEGER)
DISTRIBUTED RANDOMLY;
                      
INSERT INTO madlib_demo.patients VALUES
(1,  1, 1, 70),
(2,  1, 1, 80),
(3,  1, 1, 50),
(4,  1, 0, 60),
(5,  1, 0, 40),
(6,  1, 0, 65),
(7,  1, 0, 75),
(8,  1, 0, 80),
(9,  1, 0, 70),
(10, 1, 0, 60),
(11, 0, 1, 65),
(12, 0, 1, 50),
(13, 0, 1, 45),
(14, 0, 1, 35),
(15, 0, 1, 40),
(16, 0, 1, 50),
(17, 0, 0, 55),
(18, 0, 0, 45),
(19, 0, 0, 50),
(20, 0, 0, 60);

DROP TABLE IF EXISTS madlib_demo.patients_logregr;

SELECT madlib.logregr_train( 'madlib_demo.patients',                             -- Source table
                             'madlib_demo.patients_logregr',                     -- Output table
                             'second_attack',                        -- Dependent variable
                             'ARRAY[1, treatment, trait_anxiety]',   -- Feature vector
                             NULL,                                   -- Grouping
                             20,                                     -- Max iterations
                             'irls'                                  -- Optimizer to use
                           );


CREATE OR REPLACE FUNCTION madlib_demo.patient_prediction_batch() RETURNS integer	
AS $$ 
	begin
		
		drop table if exists madlib_demo.patient_ha_predict;
	
		create table madlib_demo.patient_ha_predict as
			SELECT p.id as patient_id, 
					madlib.logregr_predict(coef, ARRAY[1, treatment, trait_anxiety]) prediction,
	       			p.second_attack::BOOLEAN
			FROM madlib_demo.patients p, madlib_demo.patients_logregr m
			ORDER BY p.id;
		
		drop table if exists madlib_demo.patient_ha_predict_prob;
	
		create table madlib_demo.patient_ha_predict_prob as
			SELECT p.id as patient_id, 
				madlib.logregr_predict_prob(coef, ARRAY[1, treatment, trait_anxiety]) probablity,
	       		p.second_attack::BOOLEAN
			FROM madlib_demo.patients p, madlib_demo.patients_logregr m
			ORDER BY p.id;
		return 1;	
		exception
			when others then
				return -1;
	end
$$
LANGUAGE plpgsql;