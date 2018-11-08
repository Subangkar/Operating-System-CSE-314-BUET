import java.io.Serializable;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Vector;

public class Chunk implements Serializable {
    Vector<Boolean> storage;
    int size;
    String file_id;

    public Chunk(int allowed, String id) {
        storage = new Vector<>();

        size = allowed;
        file_id = id;
    }

    public void create_frame(byte[] array, byte type, byte seq_no, byte ack_no,int error_flag) {
        int payload_size = array.length;
        int n = payload_size + 4;
        byte[] temp = new byte[n];
        temp[0] = type;
        temp[1] = seq_no;
        temp[2] = ack_no;
        BitSet temp_frame = BitSet.valueOf(reverse(array));
        int count_one = 0;
        for (int i = 0; i < payload_size * 8; i++) {
            if (temp_frame.get(i) == true)
                count_one++;
        }
        if (type == 1) System.out.println("count of 1 is " + count_one);
        byte checksum;
        if (count_one % 2 == 0) checksum = 0;
        else checksum = 1;
        temp[n - 1] = checksum;
        for (int i = 0; i < payload_size; i++) {
            temp[i + 3] = array[i];
        }
        BitSet final_frame = BitSet.valueOf(reverse(temp));
        storage.add(Boolean.FALSE);
        for (int i = 0; i < 6; i++) {
            storage.add(Boolean.TRUE);
        }
        storage.add(Boolean.FALSE);
        count_one = 0;
        if (type == 1) {
            System.out.println("Before Stuffing");

            for (int i = 0; i < n * 8; i++) {
                if(i%8==0) System.out.print(" ");
                if (final_frame.get(i) == true) System.out.print(1);
                else System.out.print(0);

            }
            System.out.print("\n");
        }
        if(error_flag==1) {
            if (final_frame.get(25)) final_frame.clear(25);
            else final_frame.set(25);
        }
        for (int i = 0; i < n * 8; i++) {
            boolean b = final_frame.get(i);
            if (b == true) {
                count_one++;
                storage.add(Boolean.TRUE);
                if (count_one == 5) {
                    storage.add(Boolean.FALSE);
                    count_one = 0;
                }
            } else {
                storage.add(Boolean.FALSE);
                count_one = 0;
            }
        }
        storage.add(Boolean.FALSE);
        for (int i = 0; i < 6; i++) {
            storage.add(Boolean.TRUE);
        }
        storage.add(Boolean.FALSE);

    }

    public byte[] reverse(byte[] data) {
        byte[] bytes = data.clone();

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (Integer.reverse(bytes[i]) >>> 24);
        }

        return bytes;
    }

    public void after_bit_pattern() {

        Iterator<Boolean> it2 = storage.iterator();
        int it=0;
        while (it2.hasNext()) {
            it++;

            if (it2.next() == Boolean.TRUE) System.out.print(1);
            else System.out.print(0);
            if(it==8) {
                System.out.print(" ");
                it=0;
            }
        }
        System.out.print("\n");
    }

    public void de_stuff() {

        int i;
        int count = 0;
        Object[] array = storage.toArray();

        storage.clear();
        for (i = 8; i < (array.length - 8); i++) {
              String s=array[i].toString();
              if(s.equals("true"))
              {
                count++;
                storage.add(Boolean.TRUE);
                if (count == 5) {
                    i++;
                    count = 0;
                }
            } else {
                storage.add(Boolean.FALSE);
                count = 0;
            }
        }

    }
    public byte[] payload_array_byte()
    {
        int i;
        Object[] array = storage.toArray();

        storage.clear();
        for (i = 24; i < (array.length - 8); i++) {
            String s=array[i].toString();
            if(s.equals("true"))
            {

                storage.add(Boolean.TRUE);

            } else {
                storage.add(Boolean.FALSE);
            }
        }
        System.out.println("Only payload");
        after_bit_pattern();
        byte[] toReturn = new byte[storage.size() / 8];
        for (int entry = 0; entry < toReturn.length; entry++) {
            for (int bit = 0; bit < 8; bit++) {
                if (storage.elementAt(entry * 8 + bit)) {
                    toReturn[entry] |= (128 >> bit);
                }
            }
        }
        return toReturn;

    }
    public boolean hasCheckSumError()
    {
        Object[] array=storage.toArray();
        int count=0;
        int len=array.length;
        for(int i=24;i<(len-8);i++)
        {
            if(array[i].toString().equals("true")) count++;
        }
        int checksum;
        if(array[len-1].toString().equals("true"))
        {
            checksum=1;
        }
        else checksum=0;
        if(count%2==0 && checksum==0) return false;
        else if(count%2==1 && checksum==1) return false;
        return true;
    }

    public void create_ackn_frame(byte type,byte ackn)
    {
        size=0;
        int payload_size = size;
        int n = payload_size + 4;
        byte[] temp = new byte[n];
        temp[0] = type;
        temp[1] = 0;
        temp[2] = ackn;
        temp[n - 1] = 0;
        BitSet final_frame = BitSet.valueOf(reverse(temp));
        storage.add(Boolean.FALSE);
        for (int i = 0; i < 6; i++) {
            storage.add(Boolean.TRUE);
        }
        storage.add(Boolean.FALSE);
        int count_one = 0;
        for (int i = 0; i < n * 8; i++) {
            boolean b = final_frame.get(i);
            if (b == true) {
                count_one++;
                storage.add(Boolean.TRUE);
                if (count_one == 5) {
                    storage.add(Boolean.FALSE);
                    count_one = 0;
                }
            } else {
                storage.add(Boolean.FALSE);
                count_one = 0;
            }
        }
        storage.add(Boolean.FALSE);
        for (int i = 0; i < 6; i++) {
            storage.add(Boolean.TRUE);
        }
        storage.add(Boolean.FALSE);
    }


    public int get_ackn_no()
    {
        Object[] array = storage.toArray();
        if(array[23].toString().equals("true"))
        {
            return 1;
        }
        return 0;
    }


    public int get_seq_no()
    {
        Object[] array = storage.toArray();
        if(array[15].toString().equals("true"))
        {
            return 1;
        }
        return 0;
    }
}
