#!/usr/bin/env bash

curl --include \
  --request GET \
  -b cookies.txt -c cookies.txt \
  http://localhost:9000/ad