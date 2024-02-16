### Caching Mechanisms

Supporting Caching for Improved Performance

Caching is a crucial mechanism for improving application performance by reducing database queries and minimizing latency. 
In Bibernate, caching is supported at both the first-level and second-level.

## First-Level Caching

First-level caching, also known as session-level caching, is provided by default in Bibernate. 
It operates at the session level, meaning that within a session, if the same entity is requested multiple times, 
Bibernate will return the cached instance rather than hitting the database again.


## Second-Level Caching

Second-level caching extends caching beyond the scope of a single session. 
It allows cached data to be shared across multiple sessions and even multiple JVMs.


**Note:** Currently, in Bibernate, second-level caching is only supported for immutable entities.