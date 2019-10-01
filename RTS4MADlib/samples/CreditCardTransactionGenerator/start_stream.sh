#!/bin/bash
#-------------------------------------------------------------------------------
# Copyright 2019 Pivotal Software Inc
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
"""
    Name: fraudSignatures.py
    Author: Sridhar Paldugu
    Description: Generate a stream of transactions into Greenplum using gpfdist.
    			This code should run along with Generate.py when the files are 
    			generated to local folder. The Gnerate.py generates transaction
    			JSON files to project data folder and this script sweep the files
    			to greenplum using gpfdist.

"""

echo "starting to load data to greenplum ............"

PROJ_DIR=.
DATA_DIR=$PROJ_DIR/data
STAGE_DIR=$PROJ_DIR/stage
ARCHIVE_DIR=$PROJ_DIR/processed
GP_MASTER=
GP_USER=
GP_PASSWD=
echo "PROJ_DIR = $PROJ_DIR"
echo "DATA_DIR = $DATA_DIR"
echo "STAGE_DIR = $STAGE_DIR"

LD_LIBRARY_PATH=$PROJ_DIR/greenplum
export LD_LIBRARY_PATH

timestamp()
{
 date +"%Y-%m-%d %T"
}

while true
do
	echo "*********** STARTING NEW MICRO BATCH ********************************"
	echo "Checking if files arrived ............."
	COUNT=$(ls -l $DATA_DIR/transactions*.csv | wc -l)
	if [ $COUNT -ge 1 ]
	then
		echo "moving top 2 files to staging foler $STAGE_DIR ........"
		for file in `ls -tr $DATA_DIR/transactions*.csv | head -n 2`; do mv $file $STAGE_DIR; done
		echo "launching gpfdist ........"
		$PROJ_DIR/greenplum/gpfdist -d $STAGE_DIR/ -p 8081 > $PROJ_DIR/logs/$(date "+%Y.%m.%d-%H.%M.%S")_gpfdist.log 2>&1 &
		echo "doing parallel inserts into greenplum table .........."
		psql -h $GP_MASTER -U $GP_USER -d $GP_PASSWD -w -f $PROJ_DIR/insert_gp.sql
		echo "fetching row count ........."
		psql -h $GP_MASTER -U $GP_USER -d $GP_PASSWD -w -f $PROJ_DIR/query_gp_sql
		echo "stopping gpfdist ......"
		ps -ef | grep gpfdist | grep -v grep | awk '{print $2}' | xargs kill -9 &> /dev/null
		echo "moving processed files to archive folder $ARCHIVE_DIR ......." 
		for file in `ls -tr $STAGE_DIR/transactions*.csv | head -n 2`; do mv $file $ARCHIVE_DIR; done
		sleep 2
	else
		echo "No files arrived yet, sleeping for 10 seconds .........."
		sleep 10
	fi

	echo "*********** FINISHED MICRO BATCH ********************************"
done
