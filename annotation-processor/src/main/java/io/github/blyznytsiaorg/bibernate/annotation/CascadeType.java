package io.github.blyznytsiaorg.bibernate.annotation;

/**
 * Defines the set of cascadable operations that are propagated 
 * to the associated entity.
 * The value <code>cascade=ALL</code> is equivalent to 
 * <code>cascade={PERSIST, MERGE, REMOVE}</code>.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public enum CascadeType {

  /** Cascade all operations */
  ALL,

  /** Cascade persist operation */
  PERSIST,

  /** Cascade merge operation */
  MERGE,

  /** Cascade remove operation */
  REMOVE,
  
}
