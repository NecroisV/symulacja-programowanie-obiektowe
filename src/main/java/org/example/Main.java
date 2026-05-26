package org.example;

import java.util.concurrent.TimeUnit;
public class Main {
    public static void main(String[] args) {
        SrodowiskoSymulacji srodowiskoSymulacji = new SrodowiskoSymulacji(10, 15);

        for(int i = 0; i <= 10; i++) {
            srodowiskoSymulacji.krokSymulacji();
            try {
                TimeUnit.SECONDS.sleep(2);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}