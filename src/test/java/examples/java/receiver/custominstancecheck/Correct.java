package examples.java.receiver.custominstancecheck;

import edu.illinois.cs.cs125.jenisol.core.InstanceValidationException;
import edu.illinois.cs.cs125.jenisol.core.InstanceValidator;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class Correct {
  private int[] values;

  public Correct() {
    values = new int[8];
  }

  public int getValue() {
    return 1;
  }

  @InstanceValidator
  private static void validateInstance(Object instance) {
    for (Field field : instance.getClass().getDeclaredFields()) {
      if (field.getType().isArray()) {
        field.setAccessible(true);
        try {
          int length = Array.getLength(field.get(instance));
          if (length > 8) {
            throw new InstanceValidationException("Array is too long");
          }
        } catch (IllegalAccessException ignored) {
        }
      }
    }
  }
}
