# Bibernate Framework Documentation

<img width="602" alt="image" src="https://github.com/BlyznytsiaOrg/bring/assets/73576438/32dd414f-dc3b-4d4b-8170-348e584b556b">


## Getting Started

If you're new to Bibernate, consider initiating your experience with a [Bibernate playground application repo](https://github.com/BlyznytsiaOrg/bibernate-playground) 
with a variety of examples of how to use it.
Bibernate offers a swift and opinionated method to develop a Bibernate-based application ready for play.

## Bibernate Framework Overview

Bibernate simplifies the development of Java enterprise applications and efficient ORM (Object-Relational Mapping) framework designed
to simplify database interaction in Java applications by providing comprehensive support for leveraging the Java language within an enterprise setting. 
Bibernate 1.0 development requires Java 17.

## Prerequisites

Before getting started with Bibernate, ensure you have the following prerequisites installed:

- Java 17
- Your preferred Java IDE such as IntelliJ IDEA
- Docker
- A compatible relational database management system PostgreSQL

## Installation

//TODO

## Design Philosophy

Bibernate embodies educational principles in its design, empowering users to master reflection and architectural design.
As a leading ORM framework, Bibernate serves as a cornerstone for understanding and implementing persistence in web servlet applications.
Its robust features facilitate seamless mapping of Java objects to database tables, simplifying data access and fostering efficient development practices for teams.


## Key Features of Bibernate

# Bibernate Core

Bibernate is often described as a library that simplifies the task of mapping Java classes to relational database tables.

Bibernate takes the pain out of persistence by freeing the developer from the burden of manually writing tedious, 
repetitive, and error-prone code to flatten object graphs into database tables and reconstruct object graphs from flat SQL 
query result sets. Moreover, Bibernate significantly simplifies performance tuning by allowing developers to focus on writing
basic persistence logic first, with the ability to optimize performance later.

# Bibernate and API

Bibernate served as the inspiration behind the Java (now Jakarta) Persistence API, or JPA. 
While JPA is well-suited for production environments, for educational purposes, 
we have developed our own simplified version of the Persistence API.


We can think of the API of Bibernate in terms of three basic elements:

- The EntityManagerFactory is typically created during application startup based on configuration settings and entity mappings.
- BibernateSessionFactory is responsible for creating and managing BibernateSession instances.
- The BibernateSession is a fundamental component of Bibernate, analogous to the Session interface in Hibernate.

<img width="566" alt="image" src="https://github.com/BlyznytsiaOrg/bring/assets/73576438/cb61bdf2-c0bf-4b6a-81a3-00045a3aa3b3">

## Documentations

- There are two types of documentation: Markdown (see below) and [JavaDoc](https://github.com/BlyznytsiaOrg/bibernate-core-javadoc)


## Features:

 - [Object-Relational Mapping (ORM)](features/): Simplifies the mapping of Java objects to relational database tables and vice versa, eliminating the need for manual SQL queries.
 - [Automatic Persistence](features/): Automatically manages the lifecycle of persistent objects, tracking changes and synchronizing them with the database.
 - [Hibernate Query Language(HQL)](features/): Provides a powerful query language similar to SQL but operates on Java objects, enabling database queries using object-oriented concepts.
 - [Caching Mechanisms](features/): Supports first-level and second-level caching to improve performance by reducing database queries and minimizing latency.
 - [Transaction Management](features/): Offers built-in support for managing database transactions, ensuring data integrity and consistency across multiple operations.
 - [Lazy Loading](features/): Delays the loading of associated objects until they are explicitly accessed, improving performance by loading only what is necessary.
 - [Criteria API](features/): Allows developers to build dynamic queries programmatically and fluent interface, enhancing query flexibility
 - [Native SQL Queries:](features/): Allows execution of native SQL queries when needed, providing flexibility and compatibility with existing database schemas.
 - [Schema Generation](features/): Offers tools for generating database schemas based on entity mappings, simplifying database setup and migration.
 - [Extensibility](features/): Provides a flexible architecture that allows developers to extend and customize Bibernate functionality to meet specific application requirements.
 - [Bring Data Repository](features/): Bibernate Data Repository is a powerful feature provided by the Bring Framework that simplifies the process of interacting with databases, particularly in the context of Bibernate persistent API.
 - [Batch Processing](features/): Facilitates batch processing of database operations, improving performance by minimizing round-trips to the database.
 - [Versioning](features/): Supports versioning of entity data and implementing optimistic concurrency control.
 - [Flyway Migration Support](features/): Integrates seamlessly with Flyway migration tool, enabling database schema management and version control through declarative SQL migration scripts. This ensures consistency and reliability in database schema evolution across different environments.

# additional items:
 - [Annotation processing](features/AnnotationProcessing.md): Ensure entity validation for proper usage during compile time.
 - [Runtime entity validation](features/RuntimeEntityValidation.md): During the initialization of the application, we'll log warnings or exceptions and offer guidance on best practices for code improvement.
 - [Reflection optimization](features/ReflectionOptimization.md): We gather all the details during startup and store them for later use because reflection is slow.


## Feedback and Contributions

If you suspect an issue within the Bibernate ORM Framework or wish to propose a new feature, kindly utilize [GitHub Issues](https://github.com/BlyznytsiaOrg/bibernate/issues/new) for reporting problems or submitting feature suggestions
If you have a solution in mind or a suggested fix, you can submit a pull request on [Github](https://github.com/BlyznytsiaOrg/bibernate). In addition, please read and configure you idea to follow our [Setup Code Style Guidelines](https://github.com/BlyznytsiaOrg/bring/wiki#setup-code-style-guidelines)
