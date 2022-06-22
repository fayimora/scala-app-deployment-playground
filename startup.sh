#!/bin/sh

export LOG4J_FORMAT_MSG_NO_LOOKUPS=true
echo Running [$0]


## Post execution handler
# post_execution_handler() {
#   ## Post Execution
# }

## Sigterm Handler
sigterm_handler() {
  if [ $pid -ne 0 ]; then
    # the above if statement is important because it ensures
    # that the application has already started. Without it you
    # could attempt cleanup steps if the application failed to
    # start, causing errors.
    kill -15 "$pid"
    wait "$pid"
    post_execution_handler
  fi
  exit 143; # 128 + 15 -- SIGTERM
}

## Setup signal trap
# on callback execute the specified handler
trap 'sigterm_handler' SIGTERM


# DB migrations can be started here
# sbt flyway:migrate

## Start Process
# run process in background and record PID
/bin/sh /app/run-app &
pid="$!"

## Wait forever until app dies
wait "$pid"
return_code="$?"

## Cleanup
# post_execution_handler

# echo the return code of the application
exit $return_code