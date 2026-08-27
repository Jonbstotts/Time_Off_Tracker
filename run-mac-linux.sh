#!/usr/bin/env bash
set -e
mvn clean package
java -jar target/time-off-tracker.jar
