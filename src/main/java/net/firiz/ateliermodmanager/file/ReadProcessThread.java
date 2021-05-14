package net.firiz.ateliermodmanager.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadProcessThread extends Thread {

    private final Process process;

    ReadProcessThread(Process process) {
        this.process = process;
    }

    @Override
    public void run() {
        try (final InputStreamReader isr = new InputStreamReader(process.getInputStream());
             final BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
