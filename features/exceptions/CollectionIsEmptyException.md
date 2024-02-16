# CollectionIsEmptyException

The `CollectionIsEmptyException` class is an exception thrown when an operation or method expects a collection to
contain elements, but the provided collection is empty. It extends the [`BibernateGeneralException`](#) class.

## Description

This class extends [BibernateGeneralException](BibernateGeneralException.md) and provides a constructor to create
instances with a specified detail message.

### Constructor

1. **CollectionIsEmptyException(String message)**
    - Constructs a new `CollectionIsEmptyException` with the specified detail message.
