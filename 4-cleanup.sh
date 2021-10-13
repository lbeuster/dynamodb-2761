#!/bin/bash
set -eo pipefail

. vars

aws cloudformation delete-stack --stack-name "$CLOUDFORMATION_STACK" || true
aws cloudformation wait stack-delete-complete --stack-name="$STACK" || true

# CFN doesn't delete tables with items
aws dynamodb delete-table --table-name "$DYNAMODB_TABLE" > /dev/null || echo "Failed to delete $DYNAMODB_TABLE"
