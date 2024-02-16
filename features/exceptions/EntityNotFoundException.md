# EntityNotFoundException

The `EntityNotFoundException` class is an exception thrown to indicate that a collection of entities could not be found
in a program. This exception is typically used when attempting to retrieve or operate on a set of entities that are
expected to exist but are not found in the underlying data source.

## Description

This class extends `RuntimeException` and provides a constructor to create instances with a specified detail message.

### Constructor

1. **EntityNotFoundException(String message)**
    - Constructs a new `EntityNotFoundException` with the specified detail message.
