package examples.java.noreceiver.customtype;

import edu.illinois.cs.cs125.jenisol.core.EdgeType;
import edu.illinois.cs.cs125.jenisol.core.RandomType;
import edu.illinois.cs.cs125.jenisol.core.SimpleType;
import java.util.Random;

public class Correct {
  public static String getName(Person person) {
    return person.getName();
  }

  @SimpleType private static final Person[] SIMPLE_PEOPLE = new Person[] {new Person("Test")};
  @EdgeType private static final Person[] EDGE_PEOPLE = new Person[] {null};

  @RandomType
  private static Person randomPerson(int complexity, Random random) {
    return new Person("Test");
  }
}
