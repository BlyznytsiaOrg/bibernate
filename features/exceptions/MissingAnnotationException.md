# MissingAnnotationException

The `MissingAnnotationException` class is an exception thrown to indicate that an operation or functionality in a
program expects the presence of a specific annotation, but the required annotation is missing. This exception is
typically used when a runtime check for a particular annotation fails.

## Description

This class extends `RuntimeException` and provides a constructor to create instances with a specified detail message.

### Constructor

1. **MissingAnnotationException(String message)**
    - Constructs a new `MissingAnnotationException` with the specified detail message.
