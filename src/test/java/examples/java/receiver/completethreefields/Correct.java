package examples.java.receiver.completethreefields;

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
}
