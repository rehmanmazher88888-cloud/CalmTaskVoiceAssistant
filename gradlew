#!/bin/sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls -di "$PRG" > /dev/null
    result=$?
    if [ $result -eq 0 ]; then
        PRG=$(readlink "$PRG")
    else
        break
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
pw=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MSYS* | MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/bin/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/bin/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    if ! command -v java >/dev/null 2>&1
    then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
    fi
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$msys" && ! "$nonstop" ; then
    case $MAX_FD in #(
      *) # Fall back to using the value in /proc.
         if [ -r /proc/self/fd_max ]; then
             MAX_FD=$(awk '{print $1}' </proc/self/fd_max)
         fi
         ;;
    esac
    [ -z "$MAX_FD" ] && MAX_FD=1048576
    if [ $? -eq 0 ] ; then
        ulimit -n $MAX_FD
    else
        warn "Could not query maximum file descriptor limit"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if $darwin; then
    DEFAULT_JVM_OPTS='"-Xdock:name=$APP_NAME" "-Xdock:icon=$APP_HOME/media/dock.icns" "-Xmx64m" "-Xms64m"'
fi

# For Cygwin or MSYS, switch paths to Windows format before running java
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
    CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )

    JAVACMD=$( cygpath --windows "$JAVACMD" )

    # We build the pattern for arguments to be converted via cygpath
    ROOTDIRSRAW=`find -L / -maxdepth 3 -type d -name .gradle 2>/dev/null`
    SEP=""
    for dir in $ROOTDIRSRAW ; do
        ROOTDIRS="$ROOTDIRS$SEP$dir"
        SEP="|"
    done
    OURCYGPATTERN="(^($ROOTDIRS)$|^true$)"
    OURJAVABASE=$(dirname "$JAVACMD")
    while IFS= read -r JAVA_OPTION ; do
        if [[ "$JAVA_OPTION" =~ ^-D.* ]]; then
             JAVA_OPTS="$JAVA_OPTS \"$JAVA_OPTION\""
        fi
        if [[ "$JAVA_OPTION" =~ ^-W.* ]]; then
             JAVA_OPTS="$JAVA_OPTS \"$JAVA_OPTION\""
        fi
    done < <(echo $DEFAULT_JVM_OPTS | tr -s ' ' '\n')
    JAVA_OPTS="$JAVA_OPTS $(getopt -o '' -l '' -- "$@" 2>/dev/null | tr '\n' ' ')"
    eval set -- $JAVA_OPTS
    JAVA_OPTS=`printf %s "$JAVA_OPTS" | xargs`

    # Collapse all .jar files in explicit classpath into a single classpath entry
    function collapse_classpath {
        local IFS=:
        local jars=( $1 )
        echo "${jars[@]}" | tr ' ' '\n' | sort -u | tr '\n' ':'
    }
    CLASSPATH=$( collapse_classpath "$CLASSPATH" )

    # Now convert the arguments - kludge to limit ourselves only to /bin/sh
    i=0
    for arg in "$@" ; do
        CHECK=`echo "$arg"|egrep -c "$OURCYGPATTERN" -`
        CHECK2=`echo "$arg"|egrep -c "^-"`                                 ### Determine if an option

        if [ $CHECK -ne 0 ] && [ $CHECK2 -eq 0 ] ; then                    ### Added a path?
            arg=`cygpath --path --ignore --mixed "$arg"`
        fi
        if [ $CHECK2 -ne 0 ] ; then                                        ### Added an option?
            if [ $i -eq 0 ] ; then
                arg=`echo "$arg" | sed 's/^-*//'`                 ### Means we added a option, strip our previous 'if' clause arg
                for j in $arg ; do
                    teststring="$teststring$j "
                done
                JAVA_OPTS="$teststring"
                if [ $i -gt 0 ] ; then
                    i=$((i-1))
                fi
            else
                arg=`echo "$arg" | sed 's/^-*//'`                 ### Means we removed the leading -'s from the alternatives already
                for j in $arg ; do
                    teststring="$teststring$j "
                done
                JAVA_OPTS="$teststring"
                if [ $i -gt 0 ] ; then
                    i=$((i-1))
                fi
            fi
        fi
        i=$((i+1))
    done
    export JAVA_OPTS
    CLASSPATH=$( collapse_classpath "$CLASSPATH" )
    exec "$JAVACMD" $JAVA_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"

else
    exec "$JAVACMD" $JAVA_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"

fi
