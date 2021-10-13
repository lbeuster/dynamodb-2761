* the tests can run on AWS (default) or Localstack, to run on Localstack set `AbstractDynamoDbIT.RUN_ON_LOCALSTACK=true`
* AWS-credentials must be found in the environment
* the table (CFN-stack) can be created with `./1-deploy.sh`  and deleted with `./4-cleanup.sh`
* or to create the table during the test set the `AbstractDynamoDbIT.AUTO_CREATE_TABLE_ON_AWS=true`