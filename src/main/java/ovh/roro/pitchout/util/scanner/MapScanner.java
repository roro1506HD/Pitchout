package ovh.roro.pitchout.util.scanner;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.World;

/**
 * @author roro1506_HD
 */
public class MapScanner {

    public List<String[]> scanSigns(World world) {
        List<String[]> result = new ArrayList<>();
        File worldFile = world.getWorldFolder();
        for (File region : Objects.requireNonNull(new File(worldFile, "region").listFiles())) {
            RegionFile regionFile = new RegionFile(region);

            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    try (DataInputStream dataInputStream = regionFile.getChunkDataInputStream(x, z)) {
                        if (dataInputStream == null)
                            continue;

                        NBTTagCompound tagCompound = NBTCompressedStreamTools.a(dataInputStream);

                        NBTTagCompound level = tagCompound.getCompound("Level");

                        NBTTagList tileEntities = level.getList("TileEntities", 10);

                        for (int i = 0; i < tileEntities.size(); i++) {
                            NBTTagCompound tileEntity = tileEntities.get(i);
                            if (tileEntity.getString("id").equals("Sign")) {
                                String position = tileEntity.getInt("x") + ":" + tileEntity.getInt("y") + ":" + tileEntity.getInt("z");
                                result.add(new String[]{tileEntity.getString("Text1"), tileEntity.getString("Text2"), tileEntity.getString("Text3"), tileEntity.getString("Text4"), position});
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            regionFile.close();
        }
        return result;
    }
}