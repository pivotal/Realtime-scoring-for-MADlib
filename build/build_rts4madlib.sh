#!/bin/sh

#-----------------------------------------------------------------------------------------------
#   Copyright 2019 Pivotal Software
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#----------------------------------------------------------------------------------------------

# Author: Sridhar Paladugu 
# Email: spaladugu@pivotal.io

# Description: Create a compressed archive of the RTS4MADlib client tool

rm -f dist/rts4madlib.tar.gz

cd ../ && \
    tar -zcvf rts4madlib.tar.gz RTS4MADlib && \
    chmod 755 rts4madlib.tar.gz && \
    mv rts4madlib.tar.gz dist/
