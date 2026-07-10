# UK NETZ configuration

Table 'configuration' was created to host all configuration variables that need to be updated at runtime.

Currently, supports the following types:
- STRING
- BOOLEAN
- INTEGER

A service is provided to handle UI related configuration and features exposed in the UI through the /ui-configuration API.

All configuration properties that need to be exposed in this API will need to have 'ui.features' prefix in order for them to be automatically included in the response. In case a property is configured both in this table and the application.properties of the application the DB value will be used with higher priority.

**_NOTE:_** This table is not meant to host security related properties such as secrets. These properties should always be set through environmental variables and managed by the dev-ops team with the use of secret manager.
