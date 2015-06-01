#!/bin/sh

echo "Ensure that this script is run as root"

mkdir -v /var/rsse
mkdir -v /var/rsse/ess
mkdir -v /var/rsse/ess/db
mkdir -v /var/rsse/ess/tmp
mkdir -v /var/rsse/ess/resp

mkdir -v /var/rsse/cache
mkdir -v /var/rsse/cache/storage
