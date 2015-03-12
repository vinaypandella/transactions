#!/bin/bash

mv Dockerfile Dockerfile.orig
cp Dockerfile.test Dockerfile
docker build -t vinaypandella/transactions-tests .
mv Dockerfile.orig Dockerfile
