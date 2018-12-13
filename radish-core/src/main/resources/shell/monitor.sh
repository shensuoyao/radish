#!/bin/bash
:<<!
Get monitoring information of server
!

export LANG=en_US
export LC_TIME="POSIX"
param=$1
sar_result=$(sar -bruq -n DEV 1 1 | grep -E "(0[0-9]|1[0-9]|2[0-3])(:[0-5][0-9]){2}")

function parseToJson1() {
	local nr_t=$1;
	local i_t=$2;
	local prefix=$3;

	local key_arr=($(echo "$sar_result" | awk -v v1=$nr_t -v v2=$i_t 'NR==v1 {for(i=v2;i<=NF;i++){print $i}}'))
	local value_arr=($(echo "$sar_result" | awk -v v1=$nr_t -v v2=$i_t 'NR==((v1+1)) {for(i=v2;i<=NF;i++){print $i}}'))
	for (( i = 0; i < ${#key_arr[@]}; i++ )); do
		if [[ "$(echo "${key_arr[i]}" | grep -E "%")" ]]; then
			local key="${prefix}${key_arr[i]:1}";
			local value="${value_arr[i]}";
		elif [[ "$(echo "${key_arr[i]}" | grep -E "/s")" ]]; then
			local key="${prefix}${key_arr[i]//\/s}";
			local value="${value_arr[i]}";
		else
			local key="${prefix}${key_arr[i]}";
			local value="${value_arr[i]}";
		fi
		echo "${key}:${value}"
	done
}

function parseToJson2() {
	local prefix=$1;
	local is_show=$2;
	if [[ "$is_show" ]]; then
		is_show="1";
	else
		is_show="0";
	fi
	local key_arr=($(echo "$sar_result" | awk -v v1=$s_nr 'NR==v1 {
		for ( i = 1; i <= NF; i++ ) {
			print $i
		}
	}'))
	local value_arr=($(echo "$sar_result" | awk -v v1=$s_nr -v v2="${key_arr[0]}" '{
		if (NR>=((v1+1)) && $1""!=v2"") {
			for ( i = 2; i <= NF; i++ ) {
				print $i
			}
		} else if (NR>=((v1+1)) && $1""==v2"") {
			exit
		}
	}'))
	local v_size=$(( ${#key_arr[@]} - 1 ))
	for (( i = 0; i < ${#value_arr[@]}; i++ )); do
		if (( $(( i % v_size)) == 0 )); then
			local k_prefix="${prefix}${value_arr[i]}."
			s_nr=$(( $s_nr + 1 ))
		elif [[ "$is_show" == "0" ]]; then
			local k_index=$(( i % v_size + 1 ))
			if [[ "$(echo "${key_arr[k_index]}" | grep -E "%")" ]]; then
				local key="${k_prefix}${key_arr[k_index]:1}";
				local value="${value_arr[i]}";
			elif [[ "$(echo "${key_arr[k_index]}" | grep -E "/s")" ]]; then
				local key="${k_prefix}${key_arr[k_index]//\/s}";
				local value="${value_arr[i]}";
			else
				local key="${k_prefix}${key_arr[k_index]}";
				local value="${value_arr[i]}";
			fi
			info="${info}${key}:${value}\n"
		fi
	done
}

function getPhpAndJava() {
	local result=$(ps aux | grep -E "php|java" | grep -v grep)
	if [[ "$param" =~ "P" ]]; then
	    echo "$result" | grep "php-fpm" | awk '{sumRss+=$6;}END{print "php.rss:"sumRss;}'
	fi
	if [[ "$param" =~ "J" ]]; then
        local js_name=$(jps | grep -v Jps)
        local j_res=$(echo "$result" | grep "java");
        OLD_IFS="$IFS"
        IFS=$'\n'
        local js_array=($j_res)
        IFS="$OLD_IFS"

        for (( i = 0; i < ${#js_array[@]}; i++ )); do
            local array=(${js_array[i]});
            local name_arr=($(echo "$js_name" | grep "${array[1]}"));
            local key="java.${name_arr[1]}.rss:";
            echo "${key}${array[5]}"
        done
	fi
}
# analyze cpu information
if [[ "$param" =~ "C" ]]; then
    parseToJson1 1 3 "cpu.util." &
fi
# analyze IO
if [[ "$param" =~ "I" ]]; then
    parseToJson1 3 2 "io." &
fi
# analyze memory util
if [[ "$param" =~ "M" ]]; then
    parseToJson1 5 2 "mem." &
fi
# analyze system average load
if [[ "$param" =~ "S" ]]; then
    parseToJson1 7 2 "system." &
fi
# get physical memory php and java used
getPhpAndJava &
if [[ "$param" =~ "N" ]]; then
    s_nr=9
    parseToJson2 "network."
fi
wait
echo -e "$info" | grep -vE "(network.[^eth])|(rxpck|txpck|rxcmp|txcmp|rxmcst)"