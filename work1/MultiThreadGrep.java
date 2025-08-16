import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadGrep {

    /**
     * @param args args[0] : string to grep, args[1] : searching directory
     */
    public static void main(String[] args) {
        String str = args[0];
        File dir = new File(args[1]);
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        findDirectory(dir, str, executorService);
        executorService.shutdown();
    }

    public static void findDirectory(File dir, String str, ExecutorService executorService) {
        File[] list = dir.listFiles();
        if (list == null) return;
        Arrays.stream(list).forEach(file -> {
            if (file.isDirectory()) {
                findDirectory(file, str, executorService);
            } else {
                executorService.submit(() -> grepFile(file, str));
            }
        });
    }

    public static void grepFile(File file, String str) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(str)) {
                    System.out.printf("%s : %s\n", file.getAbsolutePath(), line);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
