#!/usr/bin/env bash

curl --include \
  --request GET \
  -b cookies.txt -c cookies.txt \
  -H "Origin: -" \
  http://jupblb.eu/
