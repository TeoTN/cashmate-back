#!/usr/bin/env bash

curl --include \
  --request GET \
  -i http://localhost:9000/transaction/$1?token=123123124
