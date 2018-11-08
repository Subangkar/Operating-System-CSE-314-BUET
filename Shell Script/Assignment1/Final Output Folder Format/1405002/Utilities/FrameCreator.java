package Utilities;

import java.util.Random;

public class FrameCreator {
    //frame format: kind, seq_no, tx_no, payload, checksum
    private byte[] payload;
    private byte[] tempFrame;

    private byte[] frame;
    private int wasteBit;
    private boolean isCorrupted;
    private byte kind;
    private byte seqNo;
    private byte txNo;

    public FrameCreator() {
        //setPayload(payload);
    }

    public byte getKind() {
        return kind;
    }

    public byte getSeqNo() {
        return seqNo;
    }

    public byte getTxNo() {
        return txNo;
    }

    public void setPayload(byte[] payload) {
        cleanUp();
        this.payload = payload;
        int tempFrameSize = this.payload.length;
        tempFrameSize += 4;
        tempFrame = new byte[tempFrameSize];
    }

    public void setFrame(byte[] frame) {
        cleanUp();
        this.frame = frame;

    }

    public void setCorrupted(boolean corrupted) {
        isCorrupted = corrupted;
    }

    private void makeCorrupted() {
        Random random = new Random();
        int index = Math.abs(random.nextInt()) % tempFrame.length;
        int position = Math.abs(random.nextInt()) % 8;
        tempFrame[index] = toggleBit(tempFrame[index], position);
    }

    public byte[] getPayload() {
        return payload;
    }

    public byte[] getFrame() {
        return frame;
    }

    private void showBits(byte[] bytes) {
        for (int i = bytes.length - 1; i >= 0; i--) {
            byte b = bytes[i];
            for (int j = 7; j >= 0; j--) {
                System.out.print(checkBit(b, j));
            }
            System.out.print(" ");
        }
        System.out.println();
    }

    private void showBits(byte b) {
        for (int i = 7; i >= 0; i--) {
            System.out.print(checkBit(b, i));
        }
        System.out.println();
    }

    //check the pos'th bit of byte b if on or off
    private int checkBit(byte b, int pos) {
        return (b & (1 << pos)) > 0 ? 1 : 0;
    }

    //set the pos'th bit of byte b
    private byte setBit(byte b, int pos) {
        return (byte) (b | (1 << pos));
    }

    //reset the pos'th bit of byte b
    byte resetBit(byte b, int pos) {
        return (byte) (b & ~(1 << pos));
    }

    //toggle the pos'th bit of byte b
    private byte toggleBit(byte b, int pos) {
        return (byte) (b ^ (1 << pos));
    }

    //calculate the checksum of the tempFrame
    private byte calculateCheckSum() {
        byte checkSum = 0;
        for (byte b : tempFrame) {
            checkSum ^= b;
        }
        return checkSum;
    }

    //count how many set of five ones
    private int countNoOf5Ones(byte[] bytes) {
        int result = 0, counter = 0;
        int length = bytes.length * 8;
        for (int i = 0; i < length; i++) {
            int index = i / 8;
            int pos = i % 8;
            if (checkBit(bytes[index], pos) == 0) {
                counter = 0;
            } else {
                counter++;
            }
            if (counter == 5) {
                result++;
                counter = 0;
            }
        }

        return result;
    }

    //set header and trailer
    private void setDelimiters() {
        frame[frame.length - 1] = (byte) (126 >> wasteBit);
        if (wasteBit > 0) {
            byte x = (byte) (126 << (8 - wasteBit));
            frame[frame.length - 2] |= x;
        }
        frame[0] = 126;
    }

    //set the kind of frame, sequence no and transmission no
    private void setTypes(byte kind, byte seqNo, byte txNo) {
        tempFrame[0] = kind;
        tempFrame[1] = seqNo;
        tempFrame[2] = txNo;
    }

    //make a temporary frame containing kind, seqNo, txNo, payload and checkSum
    private void makeEncodeTempFrame(byte kind, byte seqNo, byte txNo) {
        setTypes(kind, seqNo, txNo);
        System.arraycopy(payload, 0, tempFrame, 3, payload.length);
        tempFrame[tempFrame.length - 1] = calculateCheckSum();
    }

    private void makeDecodeTempFrame() {
        int tempFrameSize = this.frame.length;
        int fiveOnes = countNoOf5Ones(frame);
        fiveOnes -= 2;
        int noOfWasteByte = (int) Math.ceil(fiveOnes / 8.0);
        tempFrameSize -= (noOfWasteByte + 2);   //here the 2 is for the header and trailer
        tempFrame = new byte[tempFrameSize];
    }

    //do bit stuffing
    private void bitStuffing() {
        int counter = 0;
        int limit = tempFrame.length * 8;
        for (int i = 0, j = 8; i < limit; i++, j++) {
            int tIndex = i / 8, fIndex = j / 8;
            int tPos = i % 8, fPos = j % 8;

            if (checkBit(tempFrame[tIndex], tPos) == 0) {
                counter = 0;
            } else {
                counter++;
                frame[fIndex] = setBit(frame[fIndex], fPos);
            }

            if (counter == 5) {
                counter = 0;
                j++;
            }

        }
    }

    //do bit de-stuffing
    private void bitDeStuffing() {
        int counter = 0;
        int limit = (frame.length - 1) * 8;
        for (int i = 0, j = 8; i < limit; i++, j++) {
            int tIndex = i / 8, fIndex = j / 8;
            int tPos = i % 8, fPos = j % 8;

            if (checkBit(frame[fIndex], fPos) == 0) {
                counter = 0;
            } else {
                counter++;
                tempFrame[tIndex] = setBit(tempFrame[tIndex], tPos);
            }

            if (counter == 5) {
                counter = 0;
                j++;
                if (checkBit(frame[j / 8], j % 8) == 1) {
                    break;
                }
            }
            if (tIndex == tempFrame.length - 1 && tPos == 7) {
                break;
            }

        }
    }

    //make the frame
    public void makeFrame(byte kind, byte seqNo, byte txNo) {
        //showPayload();
        makeEncodeTempFrame(kind, seqNo, txNo);
        /*
        showTempFrame();*/
        if (isCorrupted) {
            makeCorrupted();
            isCorrupted = false;
            //System.out.print("Corrupted ");
            //showTempFrame();
        }
        int frameSize = tempFrame.length + 2;
        int count = countNoOf5Ones(tempFrame);
        //System.out.println("count = " + count);
        int noOfStuffByte = (int) Math.ceil(count / 8.0);
        wasteBit = noOfStuffByte * 8 - count;
        frameSize += noOfStuffByte;
        frame = new byte[frameSize];
        bitStuffing();
        setDelimiters();
        /*
        showFrame();*/
    }

    public boolean makePayload() {
        /*
        showFrame();*/
        makeDecodeTempFrame();
        bitDeStuffing();
        /*
        showTempFrame();*/
        if (calculateCheckSum() != 0) {
            //System.out.println("Checksum validation failed");
            return false;
        }
        payload = new byte[tempFrame.length - 4];
        System.arraycopy(tempFrame, 3, payload, 0, tempFrame.length - 4);
        kind = tempFrame[0];
        seqNo = tempFrame[1];
        txNo = tempFrame[2];
        /*showPayload();*/
        return true;
    }

    private void showPayload() {
        System.out.print("Payload: ");
        showBits(payload);
    }

    private void showFrame() {
        System.out.print("Frame:   ");
        showBits(frame);
    }

    private void showTempFrame() {
        System.out.print("TFrame:  ");
        showBits(tempFrame);
    }

    public void cleanUp() {
        payload = frame = tempFrame = null;
        isCorrupted = false;
        wasteBit = 0;
        kind = seqNo = txNo = 0;

    }
}
