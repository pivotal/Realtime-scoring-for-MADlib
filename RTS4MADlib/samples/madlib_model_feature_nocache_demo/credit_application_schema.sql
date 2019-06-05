/*
Credit Application Approval Model
jvawdrey@pivotal.io
*/


DO $$
BEGIN

    IF NOT EXISTS(
        SELECT schema_name
          FROM information_schema.schemata
          WHERE schema_name = 'credit'
      )
    THEN
      EXECUTE 'CREATE SCHEMA credit';
    END IF;

END
$$;


-- ***** Model Training *****

-- Load  data
DROP EXTERNAL TABLE IF EXISTS credit.credit_application_external;
CREATE EXTERNAL WEB TABLE credit.credit_application_external (
    a1 varchar(1)
   ,a2 float
   ,a3 float
   ,a4 varchar(1)
   ,a5 varchar(2)
   ,a6 varchar(2)
   ,a7 varchar(2)
   ,a8 float
   ,a9 boolean
   ,a10 boolean
   ,a11 float
   ,a12 boolean
   ,a13 varchar(1)
   ,a14 float
   ,a15 float
   ,a16 varchar(1)
) LOCATION ('http://archive.ics.uci.edu/ml/machine-learning-databases/credit-screening/crx.data')
FORMAT 'CSV'
(NULL AS '?');

-- Impute in missing values
DROP TABLE IF EXISTS credit.credit_application_data;
CREATE TABLE credit.credit_application_data AS
SELECT row_number() OVER() AS _id
      ,coalesce(a1,'b') AS a1
      ,coalesce(a2, avg(a2) OVER()) AS a2
      ,coalesce(a3, avg(a3) OVER()) AS a3
      ,coalesce(a4, 'u') AS a4
      ,coalesce(a5, 'g') AS a5
      ,coalesce(a6, 'c') AS a6
      ,coalesce(a7, 'v') AS a7
      ,coalesce(a8, avg(a8) OVER()) AS a8
      ,coalesce(a9, True) AS a9
      ,coalesce(a10, False) AS a10
      ,coalesce(a11, 0) AS a11
      ,coalesce(a12, False) AS a12
      ,coalesce(a13, 'g') AS a13
      ,coalesce(a14, avg(a14) OVER()) AS a14
      ,coalesce(a15, avg(a15) OVER()) AS a15
      ,CASE WHEN a16 = '+' THEN 1 ELSE 0 END AS a16
FROM credit.credit_application_external
DISTRIBUTED RANDOMLY;

-- Continous model inputs
DROP TABLE IF EXISTS credit.model_inputs_cont;
CREATE TABLE credit.model_inputs_cont AS
SELECT _id
      ,a16 AS approval
      ,a2
      ,a3
      ,a8
      ,a11
      ,a14
      ,a15
FROM credit.credit_application_data
DISTRIBUTED BY (_id);
SELECT * FROM credit.model_inputs_cont LIMIT 0;

-- Categorical model inputs
DROP TABLE IF EXISTS credit.model_inputs_cat;
SELECT madlib.encode_categorical_variables (
    'credit.credit_application_data',
    'credit.model_inputs_cat',
    'a1,a4,a5,a6,a7,a9,a10,a12,a13',
    NULL,
    '_id',
    NULL,
    'a1=b, a4=y, a5=p, a6=x, a7=z, a9=false, a10=false, a12=false, a13=s'
);

-- Combine model tables
DROP TABLE IF EXISTS credit.model_inputs;
CREATE TABLE credit.model_inputs AS
SELECT *
FROM credit.model_inputs_cat
JOIN credit.model_inputs_cont
USING (_id);

-- Split test and training data
DROP TABLE IF EXISTS credit.model
                    ,credit.model_train
                    ,credit.model_test;
SELECT madlib.train_test_split(
    'credit.model_inputs',
    'credit.model',
    0.7,
    NULL,
    NULL,
    '*',
    FALSE,
    TRUE
);


-- Train random forest model
DROP TABLE IF EXISTS credit.rf_model, credit.rf_model_summary, credit.rf_model_group;
SELECT madlib.forest_train(
        'credit.model_train',
        'credit.rf_model',
        '_id',
        'approval',
        'a2,a3,a8,a11,a14,a15,a1_a,a4_l,a4_u,a5_g,a5_gg,a6_aa,a6_c,a6_cc,a6_d,a6_e,a6_ff,a6_i,a6_j,a6_k,a6_m,a6_q,a6_r,a6_w,a7_bb,a7_dd,a7_ff,a7_h,a7_j,a7_n,a7_o,a7_v,a9_true,a10_true,a12_true,a13_g,a13_p',
        null,
        null,
        10::integer,
        5::integer,
        true::boolean,
        5::integer,
        10::integer,
        3::integer,
        1::integer,
        10::integer
    );


-- Score test data
DROP TABLE IF EXISTS credit.model_test_scored;
SELECT madlib.forest_predict('credit.rf_model',
                             'credit.model_test',
                             'credit.model_test_scored',
                             'prob');

-- Combine scores with original table
DROP TABLE IF EXISTS credit.model_test_scored_tmp;
CREATE TABLE credit.model_test_scored_tmp AS
SELECT *
FROM credit.model_test_scored
JOIN credit.model_test
USING (_id);
DROP TABLE credit.model_test_scored;
ALTER TABLE credit.model_test_scored_tmp RENAME TO model_test_scored;

-- Calculate AUC
DROP TABLE IF EXISTS credit.model_test_scored_auc;
SELECT madlib.area_under_roc(
    'credit.model_test_scored'
   ,'credit.model_test_scored_auc'
   ,'estimated_prob_1'
   ,'approval'
);
SELECT *
FROM credit.model_test_scored_auc;


-- Feature Engineering
DROP TABLE IF EXISTS credit.model_train;
CREATE TABLE credit.model_train AS
SELECT 1 AS _id,a2,a3,a8,a11,a14,a15,
       CASE WHEN a1 = 'a' THEN 1 ELSE 0 END AS a1_a, 
       CASE WHEN a4 = 'l' THEN 1 ELSE 0 END AS a4_l, 
       CASE WHEN a4 = 'u' THEN 1 ELSE 0 END AS a4_u, 
       CASE WHEN a5 = 'g' THEN 1 ELSE 0 END AS a5_g, 
       CASE WHEN a5 = 'gg' THEN 1 ELSE 0 END AS a5_gg, 
       CASE WHEN a6 = 'aa' THEN 1 ELSE 0 END AS a6_aa, 
       CASE WHEN a6 = 'cc' THEN 1 ELSE 0 END AS a6_cc, 
       CASE WHEN a6 = 'd' THEN 1 ELSE 0 END AS a6_d, 
       CASE WHEN a6 = 'ff' THEN 1 ELSE 0 END AS a6_ff, 
       CASE WHEN a6 = 'j' THEN 1 ELSE 0 END AS a6_j, 
       CASE WHEN a6 = 'r' THEN 1 ELSE 0 END AS a6_r, 
       CASE WHEN a6 = 'c' THEN 1 ELSE 0 END AS a6_c, 
       CASE WHEN a6 = 'e' THEN 1 ELSE 0 END AS a6_e, 
       CASE WHEN a6 = 'i' THEN 1 ELSE 0 END AS a6_i, 
       CASE WHEN a6 = 'k' THEN 1 ELSE 0 END AS a6_k, 
       CASE WHEN a6 = 'm' THEN 1 ELSE 0 END AS a6_m, 
       CASE WHEN a6 = 'q' THEN 1 ELSE 0 END AS a6_q, 
       CASE WHEN a6 = 'w' THEN 1 ELSE 0 END AS a6_w, 
       CASE WHEN a7 = 'bb' THEN 1 ELSE 0 END AS a7_bb, 
       CASE WHEN a7 = 'dd' THEN 1 ELSE 0 END AS a7_dd, 
       CASE WHEN a7 = 'ff' THEN 1 ELSE 0 END AS a7_ff, 
       CASE WHEN a7 = 'h' THEN 1 ELSE 0 END AS a7_h, 
       CASE WHEN a7 = 'j' THEN 1 ELSE 0 END AS a7_j, 
       CASE WHEN a7 = 'n' THEN 1 ELSE 0 END AS a7_n, 
       CASE WHEN a7 = 'v' THEN 1 ELSE 0 END AS a7_v, 
       CASE WHEN a7 = 'o' THEN 1 ELSE 0 END AS a7_o, 
       CASE WHEN a9 = 'True' THEN 1 ELSE 0 END AS a9_True, 
       CASE WHEN a10 = 'True' THEN 1 ELSE 0 END AS a10_True, 
       CASE WHEN a12 = 'True' THEN 1 ELSE 0 END AS a12_True, 
       CASE WHEN a13 = 'p' THEN 1 ELSE 0 END AS a13_p, 
       CASE WHEN a13 = 'g' THEN 1 ELSE 0 END AS a13_g

FROM (
  SELECT coalesce(a1,'b') AS a1
        ,coalesce(a2, avg(a2) OVER()) AS a2
        ,coalesce(a3, avg(a3) OVER()) AS a3
        ,coalesce(a4, 'u') AS a4
        ,coalesce(a5, 'g') AS a5
        ,coalesce(a6, 'c') AS a6
        ,coalesce(a7, 'v') AS a7
        ,coalesce(a8, avg(a8) OVER()) AS a8
        ,coalesce(a9, True) AS a9
        ,coalesce(a10, False) AS a10
        ,coalesce(a11, 0) AS a11
        ,coalesce(a12, False) AS a12
        ,coalesce(a13, 'g') AS a13
        ,coalesce(a14, avg(a14) OVER()) AS a14
        ,coalesce(a15, avg(a15) OVER()) AS a15
        ,CASE WHEN a16 = '+' THEN 1 ELSE 0 END AS a16
FROM (
    -- Change to select from 'message table'
    -- For example selecting random record from original data for scoring
    SELECT *
    FROM credit.credit_application_external
    ORDER BY random()
  ) foo
) bar
distributed RANDOMLY;

-- Prediction
DROP TABLE IF EXISTS credit.scored_results;

SELECT madlib.forest_predict('credit.rf_model','credit.model_inputs','credit.scored_results','prob');

SELECT estimated_prob_1, estimated_prob_0 FROM credit.scored_results limit 1;
