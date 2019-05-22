CREATE SCHEMA madlib_demo1;

CREATE TABLE madlib_demo1.patients_logregr (
	coef _float8,
	log_likelihood float8,
	std_err _float8,
	z_stats _float8,
	p_values _float8,
	odds_ratios _float8,
	condition_no float8,
	num_rows_processed int8,
	num_missing_rows_skipped int8,
	num_iterations int4,
	variance_covariance _float8
);

CREATE TABLE madlib_demo1.patients (
	id int4 NOT NULL,
	second_attack int4,
	treatment int4,
	trait_anxiety int4
);
insert into madlib_demo1.patients_logregr (coef, log_likelihood, std_err,z_stats, 
p_values, odds_ratios, condition_no, num_rows_processed, 
num_missing_rows_skipped, num_iterations, variance_covariance)
values
({-6.363469941781757,-1.0241060523932717,0.11904491666860431}, 
-9.41018298388876, 
{3.2138976637509207,1.1710784486031887,0.05497904582693064},
{-1.9799852414575607,-0.8744982486995476,2.1652779686891543},
{0.047705187069815816,0.3818469735304505,0.03036640450461887},
{0.0017233763092324992,0.3591173540549541,1.1264205122089452},
326.08192279156356, 20, 0, 5,
{{10.329138193063626,-0.4743046651957293,-0.17199590126004988},{-0.4743046651957293,1.371424732782851,-0.0011952070338160053},{-0.1719959012600499,-0.0011952070338160055,0.003022695480039739}}
);
