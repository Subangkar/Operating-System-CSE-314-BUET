package Utilities;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        byte[] payload = {-1, -1, -1, -1, -1, -1};
        Random random = new Random();
        random.nextBytes(payload);

        FrameCreator fc = new FrameCreator();
        fc.setPayload(payload);
        byte kind = (byte) random.nextInt(), seqNo = (byte) random.nextInt(), txNo = (byte) random.nextInt();
        fc.makeFrame(kind, seqNo, txNo);

        System.out.println();

        FrameCreator fc1 = new FrameCreator();
        fc1.setFrame(fc.getFrame());
        fc1.makePayload();

        System.out.println();

        //fc.setPayload(payload);
        fc.setCorrupted(true);
        fc.makeFrame(kind, seqNo, txNo);

        System.out.println();

        FrameCreator fc2 = new FrameCreator();
        fc2.setFrame(fc.getFrame());
        fc2.makePayload();
    }
}
