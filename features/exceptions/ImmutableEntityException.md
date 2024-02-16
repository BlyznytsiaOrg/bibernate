# ImmutableEntityException

The `ImmutableEntityException` class is an exception thrown to indicate an attempt to modify an immutable entity in a
Bibernate application. This exception is typically used when an operation attempts to alter an entity that is intended
to remain immutable. It extends the [`BibernateGeneralException`](#) class.

## Description

This class extends [BibernateGeneralException](BibernateGeneralException.md) and provides a constructor to create instances with a specified detail
message.

### Constructor

1. **ImmutableEntityException(String message)**
    - Constructs a new `ImmutableEntityException` with the specified detail message.
