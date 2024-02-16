# RepositoryInvocationException

The `RepositoryInvocationException` class is an exception thrown to indicate an issue related to repository method
invocation in a Bibernate application. This exception is typically used when an error occurs while invoking a method in
the repository layer, and it extends the more general exception, [`BibernateGeneralException`](#), which captures
broader exceptions within the Bibernate framework.

## Description

This class extends [BibernateGeneralException](BibernateGeneralException.md) and provides a constructor to create instances with a specified detail
message and cause.

### Constructor

1. **RepositoryInvocationException(String message, Throwable cause)**
    - Constructs a new `RepositoryInvocationException` with the specified detail message and cause.
