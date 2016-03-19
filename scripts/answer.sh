#!/usr/bin/env bash

curl --include \
  --request POST \
  --header "Content-type: application/json" \
  --data '{"questionId": 3, "answerIds": [7]}' \
  -b cookies.txt -c cookies.txt \
  http://jupblb.eu:9000/ad/4/answer?token=123123124
