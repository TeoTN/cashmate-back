#!/usr/bin/env bash

curl --include \
  --request GET \
  -b cookies.txt -c cookies.txt \
  http://jupblb.eu:9000/ad?token=123123124
