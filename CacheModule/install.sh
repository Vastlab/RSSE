#!/bin/sh

CC=gcc

if[ $(whoami) != "root" ]; then
	echo "su or sudo into root and try again."
	exit 1
fi

mkdir -v /var/rsse
#mkdir -v /var/rsse/ess
#mkdir -v /var/rsse/ess/db
#mkdir -v /var/rsse/ess/tmp
#mkdir -v /var/rsse/ess/resp

mkdir -v /var/rsse/cache
mkdir -v /var/rsse/cache/storage

cp dist/CacheModule.jar .

# Install a launch script:
cp CacheModule.jar /usr/sbin
chmod +x /usr/sbin/CacheModule.jar #Since some Java implementations complain about this
$CC cm.c -o cm
cp cm /usr/sbin/cm
chmod +x /usr/sbin/cm #Ensure it's executable

#Generate default config:
java -jar /usr/sbin/CacheModule.jar --genconfig
cp cm.conf /var/rsse/cache
rm cm.conf

# Now add a user account so that the CM can work with its own directory:
#echo 'Set user home to /var/rsse/cache'
#echo 'Set user name to rsse-cache'
#adduser -S /usr/sbin/cm rsse-cache

#In the above, the user's shell is also the cache module.
#chown -R rsse-cache /var/rsse/cache

