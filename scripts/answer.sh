#!/usr/bin/env bash

curl --include \
  --request POST \
  --header "Content-type: application/json" \
  --data '{"questionId": 2, "answerIds": [6]}' \
  -b cookies.txt -c cookies.txt \
  http://localhost:9000/ad/1/answer