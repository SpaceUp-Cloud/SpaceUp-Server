***REMOVED***!/usr/bin/env bash

PATH=$1
SERVICENAME=$2

FULLPATH="$PATH/$SERVICENAME.ini"

if [ -e "$FULLPATH" ]; then
  echo "Remove $FULLPATH"
  rm "$FULLPATH"
else
  error "Error: cannot find $FULLPATH"
fi

supervisorctl update

error() {
  echo "$@" 1>&2;
}