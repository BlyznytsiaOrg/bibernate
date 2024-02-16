# NonUniqueResultException

The `NonUniqueResultException` class is an exception thrown to indicate that a query or operation resulted in a
non-unique result in a runtime context. This exception is typically used when an expectation of a unique result is
violated, and multiple results are encountered.

## Description

This class extends `RuntimeException` and provides a constructor to create instances with a specified detail message.

### Constructor

1. **NonUniqueResultException(String message)**
    - Constructs a new `NonUniqueResultException` with the specified detail message.
