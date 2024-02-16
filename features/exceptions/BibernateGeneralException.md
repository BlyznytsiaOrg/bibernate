# BibernateGeneralException

The `BibernateGeneralException` class is the base exception for handling general runtime exceptions within a Bibernate
application. This exception is designed to wrap and propagate various runtime issues that may occur during the execution
of Bibernate-related operations.

## Description

This class extends `RuntimeException` and provides constructors to create instances with a specified detail message and
an optional cause.

### Constructors

1. **BibernateGeneralException(String message)**
    - Constructs a new `BibernateGeneralException` with the specified detail message.

2. **BibernateGeneralException(String message, Throwable cause)**
    - Constructs a new `BibernateGeneralException` with the specified detail message and cause.
