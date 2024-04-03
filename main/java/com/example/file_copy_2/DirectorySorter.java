package com.example.file_copy_2;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DirectorySorter {

    public static void main(String[] args) {
        File directory = new File("C:\\Users\\serhi\\IdeaProjects\\Pipiska"); // Adjust the path to your directory
        File[] subfolders = directory.listFiles(File::isDirectory);

        if (subfolders != null) {
            Arrays.sort(subfolders, new Comparator<File>() {
                private final Pattern pattern = Pattern.compile("\\d+");

                @Override
                public int compare(File o1, File o2) {
                    Matcher m1 = pattern.matcher(o1.getName());
                    Matcher m2 = pattern.matcher(o2.getName());
                    if (!m1.find() || !m2.find()) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    Integer i1 = Integer.parseInt(m1.group());
                    Integer i2 = Integer.parseInt(m2.group());
                    return i1.compareTo(i2);
                }
            });
            for (File folder : subfolders) {
                System.out.println(folder.getName());
            }
        } else {
            System.out.println("No subdirectories found.");
        }
    }
}
