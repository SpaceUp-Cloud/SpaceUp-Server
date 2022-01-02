***REMOVED***!/usr/bin/env bash

***REMOVED***
***REMOVED*** Copyright (c) 2022 Gino Atlas.
***REMOVED***
***REMOVED*** Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
***REMOVED***
***REMOVED*** The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
***REMOVED***
***REMOVED*** THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
***REMOVED***

***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***
***REMOVED***
***REMOVED*** Helper
***REMOVED***
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***

error() {
  echo "$@" 1>&2
}

check_param() {
  if [ -z "$2" ]; then
    error "$1 cannot be empty!"
    exit 2
  fi
}

***REMOVED*** $1 isReversed
***REMOVED*** $2 limit
***REMOVED*** $3 log
get_logs() {
  ***REMOVED*** https://stackoverflow.com/questions/6022384/bash-tool-to-get-nth-line-from-a-file
  ***REMOVED*** reversed https://unix.stackexchange.com/questions/9356/how-can-i-print-lines-from-file-backwards-without-using-tac
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

***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***
***REMOVED***
***REMOVED*** Main
***REMOVED***
***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***

***REMOVED*** -u xxx
***REMOVED*** -s myservice
***REMOVED*** -t info | error
***REMOVED*** -l -1 | x > 0
***REMOVED*** --reversed
***REMOVED*** TODO: pass log base path which might more convenient
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
  if [ -f $infoLog ] && [ -s $infoLog ]; then
    get_logs "$isReversed" "$limit" "$infoLog"
  else
    echo "Info log is empty or does not exist."
  fi
  echo "---"
  if [ -f $errorLog ] && [ -s $errorLog ]; then
    get_logs "$isReversed" "$limit" "$errorLog"
  else
    echo "Error log is empty or does not exist."
  fi
else
  if [ "$logtype" == "info" ]; then
    if [ -f $infoLog ] && [ -s $infoLog ]; then
      get_logs "$isReversed" "$limit" "$infoLog"
    else
      echo "Info log is empty or does not exist."
    fi
  fi
  echo "---"
  if [ "$logtype" == "error" ]; then
    if [ -f $errorLog ] && [ -s $errorLog ]; then
      get_logs "$isReversed" "$limit" "$errorLog"
    else
      echo "Error log is empty or does not exist."
    fi
  fi
fi