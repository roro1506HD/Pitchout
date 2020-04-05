package ovh.roro.pitchout.util.scanner;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * @author roro1506_HD
 */
class RegionFile {

    private final int[] offsets = new int[1024];
    private RandomAccessFile dataFile;
    private List<Boolean> sectorFree;

    RegionFile(File file) {
        try {
            this.dataFile = new RandomAccessFile(file, "rw");

            if (this.dataFile.length() < 4096L) {
                for (int i = 0; i < 1024; ++i)
                    this.dataFile.writeInt(0);

                for (int i = 0; i < 1024; ++i)
                    this.dataFile.writeInt(0);
            }

            if ((this.dataFile.length() & 4095L) != 0L) {
                for (int i = 0; i < (this.dataFile.length() & 4095L); ++i)
                    this.dataFile.write(0);
            }

            int length = (int) this.dataFile.length() / 4096;

            this.sectorFree = new ArrayList<>(length);

            for (int i = 0; i < length; ++i)
                this.sectorFree.add(true);

            this.sectorFree.set(0, false);
            this.sectorFree.set(1, false);
            this.dataFile.seek(0L);

            for (int i = 0; i < 1024; ++i) {
                int j = this.dataFile.readInt();
                this.offsets[i] = j;

                if (j != 0 && (j >> 8) + (j & 255) <= this.sectorFree.size()) {
                    for (int k = 0; k < (j & 255); ++k)
                        this.sectorFree.set((j >> 8) + k, false);
                }
            }

            for (int i = 0; i < 1024; ++i)
                this.dataFile.readInt();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    DataInputStream getChunkDataInputStream(int x, int z) {
        if (this.outOfBounds(x, z))
            return null;

        try {
            int offset = this.getOffset(x, z);

            if (offset == 0)
                return null;

            int j = offset >> 8;
            int k = offset & 255;

            if (j + k > this.sectorFree.size())
                return null;

            this.dataFile.seek(j * 4096L);
            int l = this.dataFile.readInt();

            if (l > 4096 * k)
                return null;
            else if (l <= 0)
                return null;

            byte type = this.dataFile.readByte();

            if (type == 1) {
                byte[] buffer = new byte[l - 1];
                this.dataFile.read(buffer);
                return new DataInputStream(new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(buffer))));
            } else if (type == 2) {
                byte[] buffer = new byte[l - 1];
                this.dataFile.read(buffer);
                return new DataInputStream(new BufferedInputStream(new InflaterInputStream(new ByteArrayInputStream(buffer))));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private boolean outOfBounds(int x, int z) {
        return x < 0 || x >= 32 || z < 0 || z >= 32;
    }

    private int getOffset(int x, int z) {
        return this.offsets[x + z * 32];
    }

    public void close() {
        try {
            this.dataFile.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}