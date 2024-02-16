# EntityStateWasChangeException

The `EntityStateWasChangeException` class is an exception thrown to indicate that the state of an entity in a Bibernate
application was changed unexpectedly. This exception is typically used when an attempt to modify the state of an entity
contradicts the expected or allowed changes. It extends the [`BibernateGeneralException`](#) class.

## Description

This class extends [BibernateGeneralException](BibernateGeneralException.md) and provides a constructor to create instances with a specified detail
message.

### Constructor

1. **EntityStateWasChangeException(String message)**
    - Constructs a new `EntityStateWasChangeException` with the specified detail message.
