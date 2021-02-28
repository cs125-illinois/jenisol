package examples.java.receiver.equalsthreefields;

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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Incorrect0)) {
      return false;
    }
    Incorrect0 other = (Incorrect0) o;
    return year == other.year && name.equals(other.name);
  }
}
