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

# $1 isReversed
# $2 limit
# $3 log
get_logs() {
  # https://stackoverflow.com/questions/6022384/bash-tool-to-get-nth-line-from-a-file
  # reversed https://unix.stackexchange.com/questions/9356/how-can-i-print-lines-from-file-backwards-without-using-tac
  if [ "$1" == "false" ]; then
    if [ "$2" -eq -1 ]; then
      cat "$3"
    else
      sed "1,${2}p;d" "$3"
    fi
  else
    if [ "$2" -eq -1 ]; then
      tac "$3"
    else
      tac "$3" | sed "1,${2}p;d"
    fi
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
# TODO: pass log base path which might more convenient
servicename=""
logtype=""
limit=-1
isReversed=false
logBaseDir=""

while [ -n "$1" ]; do
  case "$1" in
    -b)
      check_param "$1" "$2"
      logBaseDir=$2
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

logPath="$logBaseDir/$servicename"
infoLog="$logPath/$servicename.log"
errorLog="$logPath/$servicename-Err.log"

if [ "$logtype" == "both" ]; then
  get_logs "$isReversed" "$limit" "$infoLog"
  echo "---"
  get_logs "$isReversed" "$limit" "$errorLog"
else
  if [ "$logtype" == "info" ]; then
    get_logs "$isReversed" "$limit" "$infoLog"
  fi
  echo "---"
  if [ "$logtype" == "error" ]; then
    get_logs "$isReversed" "$limit" "$errorLog"
  fi
fi