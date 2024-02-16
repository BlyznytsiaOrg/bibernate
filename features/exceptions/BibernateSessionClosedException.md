# BibernateSessionClosedException

The `BibernateSessionClosedException` class is an exception thrown to indicate an attempt to perform an operation on a
closed session in a Bibernate application. This exception is typically used when an operation requires an open session,
but the session is already closed.

## Description

This class extends `RuntimeException` and provides a constructor to create instances with a specified detail message.

### Constructor

1. **BibernateSessionClosedException(String message)**
    - Constructs a new `BibernateSessionClosedException` with the specified detail message.
