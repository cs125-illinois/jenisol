package examples.java.receiver.completethreefields;

import java.util.Objects;

public class Incorrect0 {
  private final String name;
  private final String artist;
  private final int year;

  public Incorrect0(String setName, String setArtist, int setYear) {
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
    Incorrect0 that = (Incorrect0) o;
    return year == that.year
        && Objects.equals(name, that.name)
        && Objects.equals(artist, that.artist);
  }
}
