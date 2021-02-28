package examples.java.receiver.equalsthreefields;

import edu.illinois.cs.cs125.jenisol.core.FixedParameters;
import edu.illinois.cs.cs125.jenisol.core.RandomParameters;
import edu.illinois.cs.cs125.jenisol.core.Three;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Correct)) {
      return false;
    }
    Correct other = (Correct) o;
    return year == other.year && name.equals(other.name) && artist.equals(other.artist);
  }

  @FixedParameters
  private static final List<Three<String, String, Integer>> FIXED =
      Arrays.asList(
          new Three<>("8 (Circle)", "Bon Iver", 2016),
          new Three<>(null, "Bon Iver", 2016),
          new Three<>("8 (Circle)", null, 2016),
          new Three<>("Skinny Love", "Bon Iver", 2016),
          new Three<>("Skinny Love", "Some Other Artist", 2016));

  private static final String[] ARTISTS = {"Bon Iver", "Waxahatchee"}; // mutate-disable

  private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz ";

  private static String randomAlphanumericString(Random random, int maxLength) {
    int length = random.nextInt(maxLength);
    char[] characters = new char[length];
    for (int i = 0; i < characters.length; i++) {
      characters[i] = CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));
    }
    return new String(characters);
  }

  @RandomParameters
  private static Three<String, String, Integer> randomParameters(int complexity, Random random) {
    String name = ARTISTS[random.nextInt(ARTISTS.length)];
    String artist = randomAlphanumericString(random, 32);
    int year = random.nextInt(100) + 1900;
    return new Three<>(name, artist, year);
  }
}
