package io.github.blyznytsiaorg.bibernate.annotation.enumeration;

/**
 * Defines the set of cascadable operations that are propagated 
 * to the associated entity.
 * The value <code>cascade=ALL</code> is equivalent to <code>cascade={REMOVE}</code>.
 *
 *  @author Blyzhnytsia Team
 *  @since 1.0
 */
public enum CascadeType {

  /** Cascade all operations */
  ALL,

  /** Cascade remove operation */
  REMOVE,
  
}
