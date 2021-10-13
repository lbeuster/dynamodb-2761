#!/bin/bash
set -eo pipefail

. vars

TEMPLATE=cloudformation.yml

aws cloudformation deploy --template-file "$TEMPLATE" --stack-name "$CLOUDFORMATION_STACK" --capabilities CAPABILITY_NAMED_IAM "$@"
