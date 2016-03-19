#!/usr/bin/env bash

curl --include \
  --request POST \
  --header "Content-type: application/json" \
  --data '{"login": "a", "password": "a"}' \
  -b cookies.txt -c cookies.txt \
  -i http://localhost:9000/user/login
