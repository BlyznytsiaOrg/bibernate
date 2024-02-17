# Configuration Management

## Properties

To customize *the Bibernate* properties, set the desired configurations in the `bibernate.properties` file:

```properties
db.url=
db.user=
db.password=
db.maxPoolSize=
bibernate.2ddl.auto=
bibernate.flyway.enabled=
bibernate.show_sql=
bibernate.batch_size=
bibernate.collect.queries=
bibernate.secondLevelCache.enabled=
bibernate.secondLevelCache.host=
bibernate.secondLevelCache.port=
```
For auto Schema Generation the `bibernate.2ddl.auto=create` should be used. For Flyway migration `bibernate.flyway.enabled=true` is used.

**NOTE:** If both the `bibernate.flyway.enabled=true` and `bibernate.2ddl.auto=create` properties are set then a `BibernateGeneralException` will be thrown, indicating that only one of these properties can be set.

For enabling logging sql queries `bibernate.show_sql=true` should be used.

To add second level cache the following property `bibernate.secondLevelCache.enabled=true` should be set.


Here you can find default values for the properties:
```properties
db.url=jdbc:postgresql://localhost:5432/db
db.user=user
db.password=password
db.maxPoolSize=20
bibernate.2ddl.auto=none
bibernate.flyway.enabled=false
bibernate.show_sql=false
bibernate.batch_size=1
bibernate.collect.queries=false
bibernate.secondLevelCache.enabled=false
bibernate.secondLevelCache.host=localhost
bibernate.secondLevelCache.port=6379
```

## External settings
External settings could be set by passing map of key-value config settings as a parameter to
static method `withExternalConfiguration` of `Persistent` class:

```
Persistent.withExternalConfiguration(String entitiesPackageName, Map<String, String> externalBibernateSettings) 
```

or by passing a config file name as a parameter to
static method `withExternalConfiguration` of `Persistent` class:

```
Persistent.withExternalConfiguration(String entitiesPackageName, String configFileName) 
```

The reading env variables are supported in `bibernate.properties` or other config file and should be set like this `${ENV_VARIABLE_NAME}`:
```properties
db.password=${DB.PASSWORD}
```
