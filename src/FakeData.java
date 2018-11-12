import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

public class FakeData {
    private static final String SAMPLE_CSV_FILE = "./model6.csv";
    private static final double STDEV = 6.0;
	static RandomGenerator rand = new JDKRandomGenerator();

    public static void main(String[] args) throws IOException {
        try (
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(SAMPLE_CSV_FILE));

            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
        ) {
            for (int i = 0; i <750;i++) {
            	csvPrinter.printRecord("", "", "", ""+Math.round(FakeData.rand.nextGaussian()*STDEV-50.0));
            }

            csvPrinter.flush();            
        }
    }
}
