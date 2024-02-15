# Flyway Migration Support

To use Flyway Migration Support the property `bibernate.flyway.enabled=true` along with other database configurations should be set in the `bibernate.properties` file located in `resource` folder.
```properties
db.url=
db.user=
db.password=
bibernate.flyway.enabled=true
```
**NOTE:** If both the `bibernate.flyway.enabled=true` and `bibernate.2ddl.auto=create` properties are set in the `bibernate.properties` file, then a `BibernateGeneralException` will be thrown, indicating that only one of these properties can be set.

The migration scripts should be allocated in `db.migration` folder inside `resource` folder and named according to [Flyway naming convention](https://documentation.red-gate.com/fd/migrations-184127470.html).

- [Java Doc](https://blyznytsiaorg.github.io/bibernate-core-javadoc/io/github/blyznytsiaorg/bibernate/config/FlywayConfiguration.html)

### See Also

- [Schema Generation](SchemaGeneration.md)