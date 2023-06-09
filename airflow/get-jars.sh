#!/bin/bash
file="jars.txt"
while read line; do
  wget -P /opt/airflow/jars/ $line
done < "${file}"