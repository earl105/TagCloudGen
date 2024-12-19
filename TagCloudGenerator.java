import java.util.Comparator;

import components.map.Map;
import components.map.Map1L;
import components.map.Map2;
import components.queue.Queue;
import components.queue.Queue1L;
import components.set.Set;
import components.set.Set2;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.sortingmachine.SortingMachine;
import components.sortingmachine.SortingMachine1L;
import components.sortingmachine.SortingMachine2;

/**
 * A HTML Tag Cloud Generator which creates based on a user-inputted text file.
 *
 * @author Group AJ15
 *
 * @author Dylan Earl (earl.105)
 * @author Jake Maheras (maheras.3)
 * @author Rain Lin (lin.4307)
 *
 */
public final class TagCloudGenerator {

    /**
     * No argument constructor--private to prevent instantiation.
     */
    private TagCloudGenerator() {
    }

    /**
     * Separators to separate text.
     */
    static final String SEPARATORS = " ,.!?;:-(){}[]12\"/34567890@#$%^&*+=_\"--";

    /**
     * Prints the beginning of a sample HTML output to out.
     *
     * @param out
     *            - the file to be printed to.
     */
    public static void printHeader(SimpleWriter out) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("<meta charset=\"UTF-8\">");
        out.println("<meta name=\"viewport\" "
                + "content=\"width=device-width, initial-scale=1.0\">");
        out.println("<title>" + "Tag Cloud Generator - Group AJ15" + "</title>");
        out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"styles.css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class = \"header\">");
        out.println("<h1>Tag Cloud Generator</h1>");
        out.println("</div>");

    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @author Dylan Earl (Modified Method from Software 1 Project)
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position) {

        assert text != null : "Violation of: text is not null";
        assert SEPARATORS != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        if (text.isEmpty() || position >= text.length()) {
            return "";
        }

        int posCopy = position;
        StringBuilder result = new StringBuilder();

        //Check if character is a separator
        boolean isSeparator = SEPARATORS.indexOf(text.charAt(posCopy)) != -1;

        //iterate over text and append sequence
        while (posCopy < text.length()
                && (SEPARATORS.indexOf(text.charAt(posCopy)) != -1) == isSeparator) {
            result.append(text.charAt(posCopy));
            posCopy++;
        }

        return result.toString();
    }

    /**
     * Processes input from a SimpleReader, extracts all words (ignoring
     * separators), and returns them as an array.
     *
     * @param reader
     *            the SimpleReader from which to read lines
     * @return an array of words (without separators)
     */
    public static String[] extractWords(SimpleReader reader) {
        Queue<String> wordsQ = new Queue1L<>();

        while (!reader.atEOS()) {
            String line = reader.nextLine();
            int position = 0;

            // Process the line using nextWordOrSeparator method
            while (position < line.length()) {
                String wordOrSeparator = nextWordOrSeparator(line, position);

                // Only add to words if it is not a separator
                if (SEPARATORS.indexOf(wordOrSeparator.charAt(0)) == -1) {

                    wordsQ.enqueue(wordOrSeparator);
                }

                // Move position forward by the length of the wordOrSeparator
                position += wordOrSeparator.length();
            }
        }

        // Convert the queue to an array
        int length = wordsQ.length();
        String[] words = new String[(length)];

        for (int i = 0; i < length; i++) {
            words[i] = wordsQ.dequeue();
        }

        return words;
    }

    /**
     * Creates a map of all of the words found within the provided inputs. The
     * associated value of each Map.key represents how many occurrences of each
     * word there is.
     *
     * @param words
     *            - array of words to put into map
     * @return - the created map
     */
    public static Map<String, Integer> createMapFromWords(String[] words) {

        Map<String, Integer> map = new Map1L<>();

        //For every word in the array
        for (String word : words) {
            if (map.hasKey(word)) { //if word exists in map
                //increment its value
                int i = map.value(word);
                map.remove(word);
                map.add(word, i + 1);

            } else { // add it to map with default value of 1.
                map.add(word, 1);
            }

        }

        return map;
    }

    /**
     * Compare {@code String}s in lexicographic order but lowercase. This is the
     * same code as my software 1 glossary (dylan earl), but with o1 and o2
     * being lowercase so that case is ignored.
     */
    private static final class StringLT implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    }

    /**
     * Adds all the words in the word array to a sortingMachine, such that they
     * can be sorted to be printed in alphabetical order.
     *
     * @param words
     * @return the words put into a sortingMachine.
     */
    public static SortingMachine<String> createSorter(Set<String> words) {

        Comparator<String> cs = new StringLT();
        SortingMachine<String> sm = new SortingMachine1L<>(cs);

        for (String word : words) {
            sm.add(word.toLowerCase());
        }

        return sm;

    }

    /**
     * Generates and prints the HTML for a Tag Cloud based on the given map.
     *
     * @param map
     *            - the n sized map used to generate the cloud
     * @param out
     *            - the file to output to
     * @param sm
     *            - the sortingMachine with all of the words so they can be
     *            printed in alphabetical order
     * @param wordCount
     *            - the maximum amount of words to be printed.
     */
    public static void printBody(Map<String, Integer> map, SortingMachine<String> sm,
            SimpleWriter out, int wordCount) {

        //If it is in the map it exists once already
        int min = 1;
        int max = Integer.MIN_VALUE;

        //Get the largest amount of occurrences
        for (Map.Pair<String, Integer> pair : map) {
            if (pair.value() > max) {
                max = pair.value();
            }
        }

        //Set font size scaling
        final int minFontSize = 10;
        final int maxFontSize = 75;

        //Count how many words have been printed so far.
        int currWordCount = 0;
        out.println("<div style='width: 100%; text-align: center;'>");

        sm.changeToExtractionMode();

        /*
         * While there are words left and we have not reached the maximum amount
         * of words yet
         */
        while (sm.size() > 0 && currWordCount < wordCount) {

            //Grab alphabetically
            String str = sm.removeFirst();

            //If it exists in the map (it should)
            if (map.hasKey(str)) {
                //Get how many times it shows up
                int count = map.value(str);

                //Set the font size accordingly
                int fontSize = minFontSize;
                if (max > min) { //to prevent divide by 0 error
                    fontSize += (count - min) * (maxFontSize - minFontSize) / (max - min);
                }

                //Print the word with the scaled font size.
                out.println("<span style='font-size:" + fontSize + "px; margin: 5px;'>"
                        + str + "</span>");
                currWordCount++;
            } else {
                //DEBUG LINE
                System.out.println("ERROR: WORD " + str + " NOT FOUND");

            }
            //Increment word counter so we do not go over the maximum allowed count.

            //Create a newline every 10 words
            final int ten = 10;
            if (currWordCount % ten == 0) {
                out.println("<br>");
            }
        }
        out.println("</div>");
    }

    /**
     * Compares Map.Pair values.
     *
     *
     * @return the comparison
     */
    public static class MapPairComparator
            implements Comparator<Map.Pair<String, Integer>> {
        /**
         * Compares Map.Pair values.
         *
         * @param entry1
         *            entry to be compared
         *
         * @param entry2
         *            other entry to be compared
         */
        @Override
        public final int compare(Map.Pair<String, Integer> entry1,
                Map.Pair<String, Integer> entry2) {
            return entry2.value().compareTo(entry1.value());
        }
    }

    /**
     * Generates a new map of the top {@code n} entries in map.
     *
     * @param words
     *            - the array of all of the words in the user provided text
     * @param n
     *            - the number of entries to put into the new map
     * @return - a new map of the most common words up to {@code n} entries.
     */
    public static Map<String, Integer> createSizedMap(String[] words, int n) {

        Map<String, Integer> oldMap = createMapFromWords(words);
        TagCloudGenerator.MapPairComparator order = new MapPairComparator();
        SortingMachine<Map.Pair<String, Integer>> sm = new SortingMachine2<>(order);

        for (Map.Pair<String, Integer> entry : oldMap) {
            sm.add(entry);
        }
        sm.changeToExtractionMode();

        Map<String, Integer> newMap = new Map2<>();

        for (int i = 0; i < n && sm.size() > 0; i++) {
            Map.Pair<String, Integer> entry = sm.removeFirst();
            newMap.add(entry.key().toLowerCase(), entry.value());
        }

        return newMap;

    }

    /**
     * creates a css file for the html to use.
     *
     * @param out
     *            - the css file to print to
     */
    public static void printCSSHeader(SimpleWriter out) {
        out.println("body {");
        out.println("    font-family: Arial, sans-serif;");
        out.println("    margin: 0;");
        out.println("    padding: 0;");
        out.println("}");
        out.println();
        out.println(".header {");
        out.println("    display: flex;");
        out.println("    justify-content: center;");
        out.println("    align-items: center;");
        out.println("    background-color: #f0f0f0;");
        out.println("    padding: 20px;");
        out.println("    border-radius: 15px;");
        out.println("    transition: transform 0.2s ease-in-out;");
        out.println("    width: 80%;");
        out.println("    margin: 20px auto;");
        out.println("}");
        out.println();
        out.println(".header h1 {");
        out.println("    margin: 0;");
        out.println("    text-align: center;");
        out.println("    color: #333;");
        out.println("}");
        out.println();
        out.println(".header:hover {");
        out.println("    transform: scale(1.03); /* Slight zoom effect */");
        out.println("}");
    }

    /**
     * adds to the CSS file to have css for the footer content.
     *
     * @param out
     *            - the css file to print to
     */
    public static void printCSSFooter(SimpleWriter out) {
        out.println(".footer {");
        out.println("    justify-content: center;");
        out.println("    background-color: #f0f0f0;");
        out.println("    padding: 20px 40px;");
        out.println("    border-radius: 15px;");
        out.println("    transition: transform 0.2s ease-in-out;");
        out.println("    width: 80%;");
        out.println("    margin: 20px auto;");
        out.println("}");
        out.println();
        out.println(".footer:hover {");
        out.println("    transform: scale(1.03); /* Slight zoom effect */");
        out.println("}");
    }

    /**
     * Prints the ending of a sample HTML output to out.
     *
     * @param out
     *            - the file to be printed to.
     */
    public static void printCloser(SimpleWriter out) {
        out.println();
        out.println();
        out.println("<div class=\"footer\">");
        out.println("<p><a href=\"https://cse22x1.engineering.osu.edu"
                + "/2231/web-sw2/assignments/"
                + "projects/tag-cloud-generator/tag-cloud-generator1."
                + "html\">Project Instructions</a></p>");

        out.println("<p>");
        out.println("<b>  Created by:</b><br><br>");
        out.println("<li>  Dylan Earl (earl.105)</li><br>");
        out.println("<li>  Jake Maheras (maheras.3)</li><br>");
        out.println("<li>  Rain Lin (lin.4307)</li><br>");
        out.println("</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        //Get user input file name
        out.println("What is the name of your input file?");
        String fileName = in.nextLine();

        //Make sure it is valid and create the reader once it is
        SimpleReader fileReader = null;
        while (fileReader == null) {
            try {
                fileReader = new SimpleReader1L(fileName);
            } catch (RuntimeException e) {
                out.println("File not found. Please enter a valid input file name:");
                fileName = in.nextLine();
            }
        }

        //Get user output file name
        out.println("Enter an output file name: ");
        String outputFileName = in.nextLine();

        out.println("How many words should be included? We recommend 50-100.");
        int numWords = in.nextInteger();

        while (numWords <= 0) {
            out.println("Invalid amount of words. There must be greater than 0 words.");
            out.println("How many words should be included? We recommend 50-100.");
            numWords = in.nextInteger();
        }

        //Create a reader and writer for the provided file
        out.println("Reading File...");
        SimpleWriter fileWriter = new SimpleWriter1L(outputFileName);
        //  SimpleReader fileReader = new SimpleReader1L(fileName);

        out.println("Creating HTML...");
        printHeader(fileWriter);

        /*
         * Create a sized map of all the words found in the file, counting how
         * many occurrences there are of each word.
         */
        out.println("Extracting words...");
        String[] words = extractWords(fileReader);
        Map<String, Integer> map = createSizedMap(words, numWords);

        /*
         * Create a set of all of the words found in the sized map (to avoid
         * repeats), then add it to a sortingMachine so they can be
         * alphabetically sorted.
         */
        Set<String> set = new Set2<>();
        for (Map.Pair<String, Integer> pair : map) {
            if (!set.contains(pair.key().toLowerCase())) {
                set.add(pair.key().toLowerCase());
            }
        }
        SortingMachine<String> sm = createSorter(set);

        /*
         * Print map, accounting for the size of the text itself using the
         * values associated with the words. SortingMachine is passed so that
         * all words that are printed are in alphabetical order.
         */
        out.println("Updating HTML...");
        printBody(map, sm, fileWriter, numWords);

        out.println("Generating CSS...");
        SimpleWriter cssWriter = new SimpleWriter1L("styles.css");
        printCSSHeader(cssWriter);
        printCSSFooter(cssWriter);

        //Close HTML tags
        out.println("Finishing HTML...");
        printCloser(fileWriter);

        out.println("Done!");

        //Close readers/writers
        fileWriter.close();
        fileReader.close();
        in.close();
        out.close();
    }

}
