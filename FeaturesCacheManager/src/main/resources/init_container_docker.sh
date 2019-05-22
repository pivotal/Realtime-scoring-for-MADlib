#-----------------------------------------------------------------------------------------------
# Copyright 2019 Sridhar Paladugu

# Permission is hereby granted, free of charge, to any person obtaining a copy of this 
# software and associated documentation files (the "Software"), to deal in the Software 
# without restriction, including without limitation the rights to use, copy, modify, 
# merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
# permit persons to whom the Software is furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in all copies 
# or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
# INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
# PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
# FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
# OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
# OTHER DEALINGS IN THE SOFTWARE.
#----------------------------------------------------------------------------------------------

#!/bin/sh


/usr/local/madlib/bin/madpack -v -s madlib \
	--platform postgres \
	-c madlibuser/justworks!@127.0.0.1:5432/madlibdb install  > /opt/pivotal/rts-for-madlib/MADlib-install.log 2>&1


printf "Running FeaturesCacheManager Spring boot application .................."
echo "$1" > /opt/pivotal/rts-for-madlib/FeaturesCacheManager-input.log

java -jar /opt/pivotal/rts-for-madlib/FeaturesCacheManager-1.0.0-SNAPSHOT.jar \
			--spring.application.json="$1" > /opt/pivotal/rts-for-madlib/FeaturesCacheManager.log 2>&1

# java -jar -Dgemfire.deploy-working-dir=/tmp \
# 			-Dspring.profiles.active=gemfire \
# 			/opt/pivotal/madlibflow/MADlibFeaturesCacheLoader-1.0.0-SNAPSHOT.jar \
# 			--spring.application.json="$1" > /opt/pivotal/madlibflow/MADlibFeaturesCacheLoader.log 2>&1

# java -jar -Dspring.profiles.active=redis \
# 			/opt/pivotal/madlibflow/MADlibFeaturesCacheLoader-1.0.0-SNAPSHOT.jar \
# 			--spring.application.json="$1" > /opt/pivotal/madlibflow/MADlibFeaturesCacheLoader.log 2>&1
