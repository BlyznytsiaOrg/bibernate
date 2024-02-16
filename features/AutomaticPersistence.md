# Automatic Persistence

The Automatic Persistence is a core component of the bibernate framework, serving as an efficient mechanism for managing
and executing entity-related actions in Java applications. This feature simplifies the interaction between Java objects
and relational databases, offering a structured approach to handle operations like entity insertion, updating, and
deletion.

## ActionQueue interface

The Action Queue interface is a crucial component of the bibernate framework, providing a structured approach for
executing entity-related actions. This interface facilitates organized and sequential processing of actions on entities,
allowing for efficient management of database operations.

**Methods:**

- `executeEntityAction(): void`
  Executes the next entity action in the queue.
- `addEntityAction(entityAction: EntityAction): void`
  Adds an entity action to the queue for deferred execution.
- `isNotExecuted(): boolean`
  Checks if the action queue is in a state where actions should not be executed. Returns true if actions should be
  withheld, false otherwise.

## EntityAction

The EntityAction interface represents actions to be performed on entities and includes methods for execution, obtaining
the entity class, retrieving involved entities, and determining the action type.

**Methods:**

- `execute(): void`
  Executes the entity action.
- `getEntityClass(): Class<?>`
  Gets the class of the entity associated with the action.
- `getEntities(): Collection<?>`
  Gets the collection of entities involved in the action.
- `getActionType(): ActionType`
  Gets the type of action to be performed on the entities (`INSERT`, `UPDATE`, or `DELETE`).

## ActionType Enum

The ActionType enum enumerates different types of entity actions that can be performed, including INSERT, UPDATE, and
DELETE.

**Enum Values:**

- `INSERT`: Represents the action of inserting a new entity.
- `UPDATE`: Represents the action of updating an existing entity.
- `DELETE`: Represents the action of deleting an existing entity.

## Transactional Behavior

**Order of Operations:**

- Operations within a transaction are executed based on their action type.
- Notably, the DELETE operation is designed to execute as the final operation within a transaction.

**Handling Multiple Operations on the Same Entity:**

- In scenarios where multiple operations (`INSERT`, `UPDATE`, `DELETE`) are added for the same entity within a
  transaction, only the `DELETE` operation will be executed.
- If an `INSERT` operation is followed by an `UPDATE` operation on the same entity within a transaction, only
  the `INSERT` operation will be processed.

### Example: Only the delete query is executed
```java
// Given
// ... (Setup tables and Bibernate entity manager)

try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
    var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
    try (var bibernateSession = bibernateSessionFactory.openSession()) {
        var person = new Person();
        person.setId(1L);
        person.setFirstName("Rake");
        person.setLastName("Tell");
        bibernateSession.save(Person.class, person);
        
        person.setFirstName("New Rake");
        bibernateSession.update(Person.class, person);
        
        bibernateSession.delete(Person.class, person);
    }
}
```
