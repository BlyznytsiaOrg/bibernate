# NotFoundImplementationForCustomRepository

The `NotFoundImplementationForCustomRepository` class is an exception thrown to indicate that no implementation is found
for a custom repository in a Bibernate application. This exception is typically used when attempting to use a custom
repository for which no implementation is available or configured. It extends the [`BibernateGeneralException`](#)
class.

## Description

This class extends [BibernateGeneralException](BibernateGeneralException.md) and provides a constructor to create
instances with a specified detail message.

### Constructor

1. **NotFoundImplementationForCustomRepository(String message)**
    - Constructs a new `NotFoundImplementationForCustomRepository` with the specified detail message.
