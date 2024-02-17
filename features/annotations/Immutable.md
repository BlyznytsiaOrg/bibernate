# Immutable Annotation

Indicates that the annotated entity is immutable, meaning its state cannot be changed after creation.

Immutable objects are thread-safe and have several benefits, including simplified concurrency control and easier reasoning about their behavior. They are particularly useful in concurrent and distributed systems where shared mutable state can lead to complex bugs and race conditions.

### Example usage:

```java
@Immutable
public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
```