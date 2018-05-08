#!/bin/bash
echo "Starting minishift"
minishift start --memory=8Gb
echo "Login with admin/admin"
minishift console
echo "Don't forget to log to this instance using the oc login command provided by openshift"