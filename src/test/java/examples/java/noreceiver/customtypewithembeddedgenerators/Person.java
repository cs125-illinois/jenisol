package examples.java.noreceiver.customtypewithembeddedgenerators;

import edu.illinois.cs.cs125.jenisol.core.EdgeType;
import edu.illinois.cs.cs125.jenisol.core.RandomType;
import edu.illinois.cs.cs125.jenisol.core.SimpleType;
import java.util.Objects;
import java.util.Random;

public class Person {
  private String name;

  public Person(String setName) {
    name = setName;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Person person = (Person) o;
    return Objects.equals(name, person.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @SimpleType private static final Person[] SIMPLE_PEOPLE = new Person[] {new Person("Test")};
  @EdgeType private static final Person[] EDGE_PEOPLE = new Person[] {null};

  @RandomType
  private static Person randomPerson(int complexity, Random random) {
    return new Person("Test");
  }
}
