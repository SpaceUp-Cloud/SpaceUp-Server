#!/usr/bin/env bash

########################
#
# Helper
#
########################

error() {
  echo "$@" 1>&2
}

checkparam() {
  if [ -z "$2" ]; then
    error "$1 cannot be empty!"
    exit 2
  fi
}

########################
#
# Main
#
########################

# -u xxx
# -s myservice
# -t info | error
# -l -1 | x > 0
# --reversed

username=""
servicename=""
logtype=""
limit=-1
isReversed=false

while [ -n "$1" ]; do
  case "$1" in
  -u)
    checkparam "$1" "$2"
    username=$2
    shift
    ;;
  -s)
    checkparam "$1" "$2"
    servicename=$2
    shift
    ;;
  -t)
    checkparam "$1" "$2"

    lower=$(echo "$2" | tr '[:upper:]' '[:lower:]')
    if [ "$lower" = "info" ] || [ "$lower" = "error" ]; then
      logtype=$lower
    else
      error "Unknown log type! $2"
      exit 2
    fi

    shift
    ;;
  -l)
    checkparam "$1" "$2"
    limit=$2
    shift
    ;;
  --reversed)
    isReversed=true
    shift
    ;;
  *)
    error "Unknown option $1"
    exit 2
    ;;
  esac
  shift
done

echo "username: $username, servicename: $servicename, logtype: $logtype, limit: $limit"

logPath="/home/$username/logs/$servicename"
infoLog="$logPath/$servicename.log"
errorLogPath="$logPath/$servicename-error.log"

# https://stackoverflow.com/questions/6022384/bash-tool-to-get-nth-line-from-a-file
# reversed https://unix.stackexchange.com/questions/9356/how-can-i-print-lines-from-file-backwards-without-using-tac

