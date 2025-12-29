#!/bin/bash

mvn -N clean install
mvn -U clean install -DskipTests