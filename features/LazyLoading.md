# Lazy Loading

Lazy loading is a design pattern commonly used in object-relational mapping (ORM) frameworks like in Bibernate to defer the loading of associated entities or collections until they are actually accessed or requested. This can help improve performance by loading only the necessary data when needed.

## Overview

In lazy loading, the associated entities or collections are not loaded immediately along with the owning entity. Instead, they are loaded from the database only when they are accessed or referenced in the code. This can be particularly useful when dealing with large or complex object graphs, as it allows for more efficient use of resources.

## Implementation

In Java, lazy loading can be implemented using various techniques, such as proxies, bytecode enhancement, or custom handling of lazy loading logic.

## Example Using Proxies

### Entity Classes
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @Column(name = "employees_id")
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    private Address address;

    // Other fields and methods
}
```
```java
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @Column(name = "addresses_id")
    private Long id;

    @OneToOne(mappedBy = "address")
    private Employee employee;

    // Other fields and methods
}
```

### Main Class
```java
public class Main {

    public static void main(String[] args) {
        // Load an employee entity
        var persistent = createPersistent("destination.of.your.entity.classes");

        try (var bibernateEntityManager = persistent.createBibernateEntityManager()) {
            var bibernateSessionFactory = bibernateEntityManager.getBibernateSessionFactory();
            try (var bibernateSession = bibernateSessionFactory.openSession()) {
                //when
                Optional<Employee> employee = bibernateSession.findById(Employee.class, 1L);

                // At this point, the address will not be loaded
                // It will be loaded only when accessed
                Address address = employee.getAddress();
            }
        }
    }
}
```

In this example, the Employee entity has a one-to-one relationship with the Address entity, and the fetch type is set to LAZY. When an Employee entity is loaded from the database, the associated Address entity will not be loaded immediately. It will be loaded only when the getAddress() method is called on the Employee object.

## Benefits
Reduces memory consumption by loading only the necessary data.
Improves performance by avoiding unnecessary database queries.
Allows for more efficient use of resources, especially in applications with large object graphs.

## Considerations
Proper session management is essential to ensure that lazy loading works as expected.
Lazy loading might not be suitable for all use cases and should be used judiciously based on performance requirements.

## Conclusion
Lazy loading is a powerful technique used to defer the loading of associated entities or collections until they are actually needed. It can help improve performance and resource utilization in Java applications, especially when dealing with large or complex object graphs.