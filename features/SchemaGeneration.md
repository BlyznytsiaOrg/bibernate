# Schema Generation

To use auto Schema Generation the property `hibernate.2ddl.auto=create` should be set in the `bibernate.properties` file located in `resource` folder.

**NOTE:** If both the `hibernate.flyway.enabled=true` and `hibernate.2ddl.auto=create` properties are set in the `bibernate.properties` file, then a `BibernateGeneralException` will be thrown, indicating that only one of these properties can be set.



- [Java Doc]()

### See Also

- [Flyway Migration Support](FlywayMigrationSupport.md)