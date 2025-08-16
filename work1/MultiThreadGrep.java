package work1;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MultiThreadGrep {

    /**
     * @param args args[0] : string to grep, args[1] : searching directory
     */
    public static void main(String[] args) throws IOException {
        String str = args[0];
        File dir = new File(args[1]);
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        var a = System.currentTimeMillis();
        Files.walk(Path.of(args[1]))
                .filter(path -> !Files.isDirectory(path))
                .parallel()
                .flatMap(path -> {
                    try {
                        return Files.readAllLines(path, StandardCharsets.UTF_8).stream();
                    } catch (IOException e) {
                        return Stream.empty();
                    }
                })
                .filter(str::contains)
                .collect(Collectors.joining());
        System.out.println(System.currentTimeMillis() - a);

        a = System.currentTimeMillis();
        findDirectory(dir, str, executorService);
        System.out.println(System.currentTimeMillis() - a);

        a = System.currentTimeMillis();
        Files.walk(Path.of(args[1])).parallel().filter(path -> path.toFile().isFile()).forEach(path -> grepFile(path.toFile(), str));
        System.out.println(System.currentTimeMillis() - a);

        a = System.currentTimeMillis();
        findDirectory(dir, str, executorService);
        executorService.shutdown();
        System.out.println(System.currentTimeMillis() - a);
    }

    public static void findDirectory(File dir, String str, ExecutorService executorService) {
//        System.out.printf("thread %s\n", Thread.currentThread().getName());

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
//                    System.out.printf("%s : %s\n", file.getAbsolutePath(), line);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
