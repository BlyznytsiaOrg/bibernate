# UnsupportedActionTypeException

The `UnsupportedActionTypeException` class is an exception thrown to indicate that an unsupported or invalid action type
has been encountered in the Bibernate framework. This exception is typically used when attempting to process an entity
action with an unrecognized or unsupported type. It extends the [`BibernateGeneralException`](#) class.

## Description

This class extends [BibernateGeneralException](BibernateGeneralException.md) and provides a constructor to create instances with a specified detail
message.

### Constructor

1. **UnsupportedActionTypeException(String message)**
    - Constructs a new `UnsupportedActionTypeException` with the specified detail message.
