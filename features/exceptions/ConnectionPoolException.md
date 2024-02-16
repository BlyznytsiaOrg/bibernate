# ConnectionPoolException

The `ConnectionPoolException` class is an exception thrown to indicate an issue related to the connection pool in a
program. This exception is typically used when there is a problem with acquiring or managing connections from a
connection pool. It extends the `RuntimeException` class for unchecked exception handling.

## Description

This class extends `RuntimeException` and provides a constructor to create instances with a specified detail message and
cause.

### Constructor

1. **ConnectionPoolException(String message, Throwable cause)**
    - Constructs a new `ConnectionPoolException` with the specified detail message and cause.
