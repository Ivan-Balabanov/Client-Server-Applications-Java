package practice2.tiktak;

import java.util.Queue;
public class Data {

    private int state = 1;

    public int getState() {
        return state;
    }

    public synchronized void Tic() {
        try {
            while (state != 1) {
                wait();
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.print("Tic-");
        state = 2;
        notifyAll();
    }

    public synchronized void Tak() {

        try {
            while (state != 2) {
                wait();
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        System.out.print("Tak");
        state = 3;
        notifyAll();
    }

    public synchronized void Toe() {

        try {
            while (state != 3) {
                wait();
            }
        } catch (InterruptedException e){
            e.printStackTrace();
        }

        System.out.println("-toe");
        state = 1;
        notifyAll();
    }
}