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

DROP TABLE IF EXISTS madlib_demo.rf_golf CASCADE;

CREATE TABLE madlib_demo.rf_golf (
    id integer NOT NULL,
    "OUTLOOK" text,
    temperature double precision,
    humidity double precision,
    "Temp_Humidity" double precision[],
    clouds_airquality text[],
    windy boolean,
    class text
);

INSERT INTO madlib_demo.rf_golf VALUES
(1,'sunny', 85, 85, ARRAY[85, 85],ARRAY['none', 'unhealthy'], 'false','Don''t Play'),
(2, 'sunny', 80, 90, ARRAY[80, 90], ARRAY['none', 'moderate'], 'true', 'Don''t Play'),
(3, 'overcast', 83, 78, ARRAY[83, 78], ARRAY['low', 'moderate'], 'false', 'Play'),
(4, 'rain', 70, 96, ARRAY[70, 96], ARRAY['low', 'moderate'], 'false', 'Play'),
(5, 'rain', 68, 80, ARRAY[68, 80], ARRAY['medium', 'good'], 'false', 'Play'),
(6, 'rain', 65, 70, ARRAY[65, 70], ARRAY['low', 'unhealthy'], 'true', 'Don''t Play'),
(7, 'overcast', 64, 65, ARRAY[64, 65], ARRAY['medium', 'moderate'], 'true', 'Play'),
(8, 'sunny', 72, 95, ARRAY[72, 95], ARRAY['high', 'unhealthy'], 'false', 'Don''t Play'),
(9, 'sunny', 69, 70, ARRAY[69, 70], ARRAY['high', 'good'], 'false', 'Play'),
(10, 'rain', 75, 80, ARRAY[75, 80], ARRAY['medium', 'good'], 'false', 'Play'),
(11, 'sunny', 75, 70, ARRAY[75, 70], ARRAY['none', 'good'], 'true', 'Play'),
(12, 'overcast', 72, 90, ARRAY[72, 90], ARRAY['medium', 'moderate'], 'true', 'Play'),
(13, 'overcast', 81, 75, ARRAY[81, 75], ARRAY['medium', 'moderate'], 'false', 'Play'),
(14, 'rain', 71, 80, ARRAY[71, 80], ARRAY['low', 'unhealthy'], 'true', 'Don''t Play');

DROP TABLE IF EXISTS madlib_demo.rf_train_output;
DROP TABLE IF EXISTS madlib_demo.rf_train_output_group;
DROP TABLE IF EXISTS madlib_demo.rf_train_output_summary;

SELECT madlib.forest_train('madlib_demo.rf_golf',         -- source table
                           'madlib_demo.rf_train_output',    -- output model table
                           'id',              -- id column
                           'class',           -- response
                           '"OUTLOOK", temperature, humidity, windy',   -- features
                           NULL,              -- exclude columns
                           NULL,              -- grouping columns
                           20::integer,       -- number of trees
                           2::integer,        -- number of random features
                           TRUE::boolean,     -- variable importance
                           1::integer,        -- num_permutations
                           8::integer,        -- max depth
                           3::integer,        -- min split
                           1::integer,        -- min bucket
                           10::integer        -- number of splits per continuous variable
                           );

