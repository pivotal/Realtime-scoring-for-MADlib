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
