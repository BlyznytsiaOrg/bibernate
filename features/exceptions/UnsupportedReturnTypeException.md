# UnsupportedReturnTypeException

The `UnsupportedReturnTypeException` class is an exception thrown to indicate that an unsupported or invalid return type
has been encountered in the Bibernate framework. This exception is typically used when attempting to process an entity
action with a return type that is not recognized or supported.

## Description

This class extends [BibernateGeneralException](BibernateGeneralException.md) and provides a constructor to create instances with a specified detail
message.

### Constructor

1. **UnsupportedReturnTypeException(String message)**
    - Constructs a new `UnsupportedReturnTypeException` with the specified detail message.
