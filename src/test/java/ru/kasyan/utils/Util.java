package ru.kasyan.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Kasyanenko Konstantin
 * on 03.09.2017.
 */
public class Util {
    private static final String VALID_SIGNS="abcdefghijklmnopqrstuvwxyzABCEDFGHIJKLMNOPQRSTUVWXYZ";

    public static File writeStringToFile(String string, File file) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")) {
            writer.write(string);
        }
        return file;
    }

    public static String readFiles(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    public static List<File> getListFiles(String str) {
        List<File> list = new ArrayList<>();
        File f = new File(str);
        for (File s : f.listFiles()) {
            if (s.isFile()) {
                list.add(s);
            } else if (s.isDirectory()) {
                getListFiles(s.getAbsolutePath());
            }
        }
        return list;
    }

    public static String getRandomStr(int length) {
        StringBuilder sb = new StringBuilder();
        Random rnd = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int len = VALID_SIGNS.length();
            int random = rnd.nextInt(len);
            char c = VALID_SIGNS.charAt(random);
            sb.append(c);
        }
        return sb.toString();
    }
}
