package examples.java.receiver.completethreefields;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.Three;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Correct {
  private final String name;
  private final String artist;
  private final int year;

  public Correct(String setName, String setArtist, int setYear) {
    assert setName != null;
    assert setArtist != null;
    name = setName;
    artist = setArtist;
    year = setYear;
  }

  public String getName() {
    return name;
  }

  public String getArtist() {
    return artist;
  }

  public int getYear() {
    return year;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Correct correct = (Correct) o;
    return Objects.equals(name, correct.name) && Objects.equals(artist, correct.artist);
  }

  @FixedParameters
  private static final List<Three<String, String, Integer>> FIXED =
      Arrays.asList(
          new Three<>("Test", "Me", 1988),
          new Three<>("Test", "Me", 1987),
          new Three<>("Testing", "Me", 1988),
          new Three<>("Test", "You", 1988),
          new Three<>(null, "Me", 1988),
          new Three<>("Test", null, 1988));
}
