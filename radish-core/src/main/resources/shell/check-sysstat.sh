#!/bin/bash
:<<!
check if sysstat installed, if no then install this
!

export LANG=en_US
export LC_TIME="POSIX"
check_result=$(yum list installed | grep sysstat)
if [[ -z $check_result ]]; then
	yum -y install sysstat
else
	echo 1
	exit
fi
#check if installed
check_result=$(yum list installed | grep sysstat)
if [[ $check_result ]]; then
	echo 1
else
	echo 0
fi