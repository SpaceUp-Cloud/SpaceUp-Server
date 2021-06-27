#!/usr/bin/env bash

########################
#
# Helper
#
########################

error() {
  echo "$@" 1>&2
}

check_param() {
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
      check_param "$1" "$2"
      username=$2
      shift
      ;;
    -s)
      check_param "$1" "$2"
      servicename=$2
      shift
      ;;
    -t)
      check_param "$1" "$2"
      lower=$(echo "$2" | tr '[:upper:]' '[:lower:]')
      if [ "$lower" = "info" ] || [ "$lower" = "error" ] || [ "$lower" = "both" ]; then
        logtype=$lower
      else
        error "Unknown log type! $2"
        exit 2
      fi
      shift
      ;;
    -l)
      check_param "$1" "$2"
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

#echo "username: $username, servicename: $servicename, logtype: $logtype, limit: $limit, isReversed: $isReversed"

logPath="/home/$username/logs/$servicename"
infoLog="$logPath/$servicename.log"
errorLog="$logPath/$servicename-Err.log"

# https://stackoverflow.com/questions/6022384/bash-tool-to-get-nth-line-from-a-file
# reversed https://unix.stackexchange.com/questions/9356/how-can-i-print-lines-from-file-backwards-without-using-tac
if [ "$logtype" == "both" ]; then
  if [ $isReversed == "false" ]; then
    if [ "$limit" -eq -1 ]; then
      cat "$infoLog"
      echo "---"
      cat "$errorLog"
    else
      sed "1,${limit}p;d" "$infoLog"
      echo "---"
      sed "1,${limit}p;d" "$errorLog"
    fi
  else
    if [ "$limit" -eq -1 ]; then
      tac "$infoLog"
      echo "---"
      tac "$errorLog"
    else
      tac "$infoLog" | sed "1,${limit}p;d"
      echo "---"
      tac "$errorLog" | sed "1,${limit}p;d"
    fi
  fi
fi