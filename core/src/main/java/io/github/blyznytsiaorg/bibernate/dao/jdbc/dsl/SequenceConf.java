package io.github.blyznytsiaorg.bibernate.dao.jdbc.dsl;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class SequenceConf {

  private Queue<Long> ids = new LinkedList<>();
  private static final int DEFAULT_INITIAL_VALUE = 1;
  private static final int DEFAULT_ALLOCATION_SIZE = 1;
  public static final String DEFAULT_SEQ_TEMPLATE = "%s_%s_seq"; //tableName_columnName_seq

  @Getter
  private final String name;
  private final int initialValue;
  private final int allocationSize;

  public SequenceConf(String name) {
    this.name = name;
    this.initialValue = DEFAULT_INITIAL_VALUE;
    this.allocationSize = DEFAULT_ALLOCATION_SIZE;
  }

  public Long getNextId() {
    return ids.poll();
  }

  public void setNextPortionOfIds(Long currentId) {
    for (long i = currentId - allocationSize + 1; i <= currentId ; i++) {
      ids.add(i);
    }
  }




}
