# Bibernate Framework Documentation

<div style="text-align:center;">
  <img width="602" alt="image" src="https://github.com/BlyznytsiaOrg/bring/assets/73576438/32dd414f-dc3b-4d4b-8170-348e584b556b">
</div>


## Getting Started

If you're new to *the Bibernate*, consider initiating your experience with a [Bibernate Playground Application](https://github.com/BlyznytsiaOrg/bibernate-playground) 
with a variety of examples of how to use it.
*The Bibernate* offers a swift and opinionated method to develop a Bibernate-based application ready for play.

## Bibernate Framework Overview

*The Bibernate* simplifies the development of Java enterprise applications and efficient ORM (Object-Relational Mapping) framework designed
to simplify database interaction in Java applications by providing comprehensive support for leveraging the Java language within an enterprise setting. 
Bibernate 1.0 development requires Java 17.

## Prerequisites

Before getting started with *the Bibernate*, ensure you have the following prerequisites installed:

- Java 17
- Your preferred Java IDE such as IntelliJ IDEA
- Docker
- A compatible relational database management system PostgreSQL

## Installation

- Open your Maven Project:

Open the Maven project where you want to add the Bibernate framework dependencies.

- Edit pom.xml:

Locate the pom.xml file in your project.

- Add Repository Configuration:

Inside the <repositories> section of your pom.xml, add the following repository configuration
if you want to try the latest Snapshot version of Bibernate project:

```
  <repositories>
    <repository>
      <releases>
        <enabled>false</enabled>
        <updatePolicy>always</updatePolicy>
        <checksumPolicy>warn</checksumPolicy>
      </releases>
      <snapshots>
        <enabled>true</enabled>
        <updatePolicy>never</updatePolicy>
        <checksumPolicy>fail</checksumPolicy>
      </snapshots>
      <name>Nexus Snapshots</name>
      <id>snapshots-repo</id>
      <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
      <layout>default</layout>
    </repository>
  </repositories>
```

This configuration informs Maven about the repository location where it can find the Bibernate framework artifacts.

- Include Dependency:

Within the <dependencies> section of your pom.xml, add *the Bibernate* framework dependency (You will have core & annotation-processor):

```
    <dependency>
      <groupId>io.github.blyznytsiaorg.bibernate</groupId>
      <artifactId>core</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>

```


## Design Philosophy

*The Bibernate* embodies educational principles in its design, empowering users to master reflection and architectural design.
As a ORM framework, *the Bibernate* serves as a cornerstone for understanding and implementing persistence in web applications.
Its robust features facilitate seamless mapping of Java objects to database tables, simplifying data access and fostering efficient development practices for teams.


## Key Features

# Bibernate Core

*The Bibernate* can be described as a library that simplifies the issue of mapping Java classes to relational database tables.

*The Bibernate* takes the pain out of persistence by freeing the developer from the burden of manually writing tedious, 
repetitive, and error-prone code to flatten object graphs into database tables and reconstruct object graphs from flat SQL 
query result sets. Moreover, *the Bibernate* significantly simplifies performance tuning by allowing developers to focus on writing
basic persistence logic first, with the ability to optimize performance later.

# Bibernate and API

*The Bibernate* served as the inspiration behind the Java (now Jakarta) Persistence API, or JPA. 
While JPA is well-suited for production environments, for educational purposes, 
we have developed our own simplified version of the Persistence API.


*The Bibernate* API can be described in terms of three basic elements:

- The `EntityManagerFactory` is typically created during application startup based on configuration settings and entity mappings.
- The `BibernateSessionFactory` is responsible for creating and managing BibernateSession instances.
- The `BibernateSession` is a fundamental component of Bibernate, analogous to the `Session` interface in `Hibernate`.

<img width="566" alt="image" src="https://github.com/BlyznytsiaOrg/bring/assets/73576438/cb61bdf2-c0bf-4b6a-81a3-00045a3aa3b3">

## Documentation

- There are two types of documentation: Markdown (see below) and [JavaDoc](https://github.com/BlyznytsiaOrg/bibernate-core-javadoc)


## Features:

 - [Object-Relational Mapping (ORM)](features/ObjectRelationalMapping.md): Simplifies the mapping of Java objects to relational database tables and vice versa, eliminating the need for manual SQL queries.
 - [Automatic Persistence](features/AutomaticPersistence.md): Automatically manages the lifecycle of persistent objects, tracking changes and synchronizing them with the database.
 - [Bibernate Query Language(BQL)](features/BQL.md): Provides a powerful query language similar to SQL but operates on Java objects, enabling database queries using object-oriented concepts.
 - [Caching Mechanisms](features/CachingMechanisms.md): Supports first-level and second-level caching to improve performance by reducing database queries and minimizing latency.
 - [Transaction Management](features/): Offers built-in support for managing database transactions, ensuring data integrity and consistency across multiple operations.
 - [Lazy Loading](features/LazyLoading.md): Delays the loading of associated objects until they are explicitly accessed, improving performance by loading only what is necessary.
 - [Criteria API](features/CriteriaApi.md): Allows developers to build dynamic queries programmatically and fluent interface, enhancing query flexibility
 - [Native SQL Queries:](features/NativeSQLQueries.md): Allows execution of native SQL queries when needed, providing flexibility and compatibility with existing database schemas.
 - [Schema Generation](features/): Offers tools for generating database schemas based on entity mappings, simplifying database setup.
 - [Extensibility](features/Extensibility.md): Provides a flexible architecture that allows developers to extend and customize Bibernate functionality to meet specific application requirements.
 - [Bibernate Data Repository](features/BibernateDataRepository.md): Bibernate Data Repository is a powerful feature provided by the Bring Framework that simplifies the process of interacting with databases, particularly in the context of Bibernate persistent API.
 - [Batch Processing](features/): Facilitates batch processing of database operations, improving performance by minimizing round-trips to the database.
 - [Versioning](features/Versioning.md): Supports versioning of entity data and implementing optimistic concurrency control.
 - [Flyway Migration Support](features/FlywayMigrationSupport.md): Integrates seamlessly with Flyway migration tool, enabling database schema management and version control through declarative SQL migration scripts. This ensures consistency and reliability in database schema evolution across different environments.
 - [Batch Processing](features/BatchProcessing.md): Facilitates batch processing of database operations, improving performance by minimizing round-trips to the database.

**Annotations:**
 - [@Column](features/annotations/Column.md)
 - [@Table](features/annotations/Table.md)
 - [@GeneratedValue](features/annotations/GeneratedValue.md)
 - [@SequenceGenerator](features/annotations/SequenceGenerator.md)
 - [@JoinColumn](features/annotations/JoinColumn.md)
 - [@Version](features/annotations/Version.md)
 - [@Param](features/annotations/Param.md)
 - [@Query](features/annotations/Query.md)
 - [@DynamicUpdate](features/annotations/DynamicUpdate.md)
 - [@OneToOne](features/annotations/OneToOne.md)
 - [@OneToMany](features/annotations/OneToMany.md)
 - [@ManyToOne](features/annotations/ManyToOne.md)
 - [@ManyToMany](features/annotations/ManyToMany.md)

**Exceptions:**
  - [BibernateDataSourceException](features/exceptions/BibernateDataSourceException.md)
  - [BibernateGeneralException](features/exceptions/BibernateGeneralException.md)
  - [BibernateSessionClosedException](features/exceptions/BibernateSessionClosedException.md)
  - [BibernateValidationException](features/exceptions/BibernateValidationException.md)
  - [ClassLimitationCreationException](features/exceptions/ClassLimitationCreationException.md)
  - [CollectionIsEmptyException](features/exceptions/CollectionIsEmptyException.md)
  - [ConnectionPoolException](features/exceptions/ConnectionPoolException.md)
  - [EntitiesNotFoundException](features/exceptions/EntitiesNotFoundException.md)
  - [EntityNotFoundException](features/exceptions/EntityNotFoundException.md)
  - [EntityStateWasChangeException](features/exceptions/EntityStateWasChangeException.md)
  - [FailedToMatchPropertyException](features/exceptions/FailedToMatchPropertyException.md)
  - [ImmutableEntityException](features/exceptions/ImmutableEntityException.md)
  - [MappingException](features/exceptions/MappingException.md)
  - [MissingAnnotationException](features/exceptions/MissingAnnotationException.md)
  - [MissingRequiredParametersInMethod](features/exceptions/MissingRequiredParametersInMethod.md)
  - [NonUniqueResultException](features/exceptions/NonUniqueResultException.md)
  - [NotFoundImplementationForCustomRepository](features/exceptions/NotFoundImplementationForCustomRepository.md)
  - [RepositoryInvocationException](features/exceptions/RepositoryInvocationException.md)
  - [UnsupportedActionTypeException](features/exceptions/UnsupportedActionTypeException.md)
  - [UnsupportedDataTypeException](features/exceptions/UnsupportedDataTypeException.md)
  - [UnsupportedReturnTypeException](features/exceptions/UnsupportedReturnTypeException.md)

# Additional items:
 - [Annotation processing](features/AnnotationProcessing.md): Ensure entity validation for proper usage during compile time.
 - [Runtime Entity validation](features/RuntimeEntityValidation.md): During the initialization of the application, we'll log warnings or exceptions and offer guidance on best practices for code improvement.
 - [Reflection optimization](features/ReflectionOptimization.md): We gather all the details during startup and store them for later use because reflection is slow.


## Feedback and Contributions

If you suspect an issue within *the Bibernate ORM Framework* or wish to propose a new feature, kindly utilize [GitHub Issues](https://github.com/BlyznytsiaOrg/bibernate/issues/new) for reporting problems or submitting feature suggestions
If you have a solution in mind or a suggested fix, you can submit a pull request on [Github](https://github.com/BlyznytsiaOrg/bibernate). In addition, please read and configure you idea to follow our [Setup Code Style Guidelines](https://github.com/BlyznytsiaOrg/bring/wiki#setup-code-style-guidelines)
