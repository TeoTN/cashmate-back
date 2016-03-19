#!/usr/bin/env bash

curl --include \
  --request GET \
  -b cookies.txt -c cookies.txt \
  -i http://localhost:9000/coupons
