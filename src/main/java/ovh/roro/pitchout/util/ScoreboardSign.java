package ovh.roro.pitchout.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ovh.roro.pitchout.game.player.GamePlayer;

public class ScoreboardSign {

    private final VirtualTeam[] lines = new VirtualTeam[15];
    private final GamePlayer player;
    private boolean created = false;
    private String objectiveName;

    public ScoreboardSign(GamePlayer player, String objectiveName) {
        this.player = player;
        this.objectiveName = objectiveName;
    }

    private static void setField(Object edit, String fieldName, Object value) {
        try {
            Field field = edit.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(edit, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void create() {
        if (this.created)
            return;

        this.player.sendPackets(createObjectivePacket(0, this.objectiveName), setObjectiveSlot());

        int i = 0;
        while (i < this.lines.length)
            sendLine(i++);

        this.created = true;
    }

    public void destroy() {
        if (!this.created)
            return;

        this.player.sendPackets(createObjectivePacket(1, null));

        for (VirtualTeam team : this.lines)
            if (team != null)
                this.player.sendPackets(team.removeTeam());

        this.created = false;
    }

    public void setObjectiveName(String name) {
        this.objectiveName = name;

        if (this.created)
            this.player.sendPackets(createObjectivePacket(2, name));
    }

    public void setLine(int line, String value) {
        getOrCreateTeam(line).setValue(value);
        sendLine(line);
    }

    public void removeLine(int line) {
        VirtualTeam team = getOrCreateTeam(line);
        String old = team.getCurrentPlayer();

        if (old != null && this.created)
            this.player.sendPackets(removeLine(old), team.removeTeam());

        this.lines[line] = null;
    }

    public void clearLines() {
        for (int i = 0; i < this.lines.length; i++)
            if (this.lines[i] != null)
                this.removeLine(i);
    }

    public String getLine(int line) {
        if (line > 14 || line < 0)
            return null;
        return getOrCreateTeam(line).getValue();
    }

    public VirtualTeam getTeam(int line) {
        if (line > 14 || line < 0)
            return null;
        return getOrCreateTeam(line);
    }

    public String[] getLines() {
        return Arrays.stream(this.lines)
                .filter(Objects::nonNull)
                .map(VirtualTeam::getValue)
                .toArray(String[]::new);
    }

    private void sendLine(int line) {
        if (line > 14 || line < 0)
            return;
        if (!this.created)
            return;

        VirtualTeam team = getOrCreateTeam(line);

        for (Packet<PacketListenerPlayOut> packet : team.sendLine())
            this.player.sendPackets(packet);

        this.player.sendPackets(sendScore(team.getCurrentPlayer(), line));
        team.reset();
    }

    private VirtualTeam getOrCreateTeam(int line) {
        if (this.lines[line] == null)
            this.lines[line] = new VirtualTeam("__fakeScore" + line, line);

        return this.lines[line];
    }

    private PacketPlayOutScoreboardObjective createObjectivePacket(int mode, String displayName) {
        PacketPlayOutScoreboardObjective packet = new PacketPlayOutScoreboardObjective();

        setField(packet, "a", this.player.getName());
        setField(packet, "d", mode);

        if (mode == 0 || mode == 2) {
            setField(packet, "b", displayName);
            setField(packet, "c", IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
        }

        return packet;
    }

    private PacketPlayOutScoreboardDisplayObjective setObjectiveSlot() {
        PacketPlayOutScoreboardDisplayObjective packet = new PacketPlayOutScoreboardDisplayObjective();

        setField(packet, "a", 1);
        setField(packet, "b", this.player.getName());

        return packet;
    }

    private PacketPlayOutScoreboardScore sendScore(String line, int score) {
        PacketPlayOutScoreboardScore packet = new PacketPlayOutScoreboardScore(line);

        setField(packet, "b", this.player.getName());
        setField(packet, "c", score);
        setField(packet, "d", PacketPlayOutScoreboardScore.EnumScoreboardAction.CHANGE);

        return packet;
    }

    private PacketPlayOutScoreboardScore removeLine(String line) {
        return new PacketPlayOutScoreboardScore(line);
    }

    enum Symbols {
        A(''),
        B(''),
        C(''),
        D(''),
        E(''),
        F(''),
        G(''),
        H(''),
        I(''),
        J(''),
        K(''),
        L(''),
        M(''),
        N(''),
        O('');

        char cha;

        Symbols(char cha) {
            this.cha = cha;
        }

        @Override
        public String toString() {
            return cha + "";
        }
    }

    /**
     * This class is used to manage the content of a line. Advanced users can use it as they want, but they are
     * encouraged to read and understand the
     * code before doing so. Use these methods at your own risk.
     */
    static class VirtualTeam {

        private final String name;
        private String prefix;
        private String suffix;
        private String currentPlayer;

        private boolean prefixChanged, suffixChanged = false;
        private boolean first = true;

        private int line;

        private VirtualTeam(String name, String prefix, String suffix, int line) {
            this.name = name;
            this.prefix = prefix;
            this.suffix = suffix;
            this.line = line;
            this.currentPlayer = Symbols.values()[line].toString();
        }

        private VirtualTeam(String name, int line) {
            this(name, "", "", line);
        }

        public String getName() {
            return this.name;
        }

        String getPrefix() {
            return this.prefix;
        }

        void setPrefix(String prefix) {
            if (this.prefix == null || !this.prefix.equals(prefix))
                this.prefixChanged = true;

            this.prefix = prefix;
        }

        public int getLine() {
            return this.line;
        }

        String getSuffix() {
            return this.suffix;
        }

        void setSuffix(String suffix) {
            if (this.suffix == null || !this.suffix.equals(this.prefix))
                this.suffixChanged = true;

            this.suffix = suffix;
        }

        private PacketPlayOutScoreboardTeam createPacket(int mode) {
            PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
            setField(packet, "a", this.name);
            setField(packet, "h", mode);
            setField(packet, "b", "");
            setField(packet, "c", this.prefix);
            setField(packet, "d", this.suffix);
            setField(packet, "i", 0);
            setField(packet, "e", "always");
            setField(packet, "f", 0);

            return packet;
        }

        PacketPlayOutScoreboardTeam createTeam() {
            return createPacket(0);
        }

        PacketPlayOutScoreboardTeam updateTeam() {
            return createPacket(2);
        }

        PacketPlayOutScoreboardTeam removeTeam() {
            PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
            setField(packet, "a", this.name);
            setField(packet, "h", 1);
            this.first = true;
            return packet;
        }

        Iterable<PacketPlayOutScoreboardTeam> sendLine() {
            List<PacketPlayOutScoreboardTeam> packets = new ArrayList<>();

            if (this.first)
                packets.add(createTeam());
            else if (this.prefixChanged || this.suffixChanged)
                packets.add(updateTeam());

            if (this.first)
                packets.add(changePlayer());

            if (this.first)
                this.first = false;

            return packets;
        }

        void reset() {
            this.prefixChanged = false;
            this.suffixChanged = false;
        }

        PacketPlayOutScoreboardTeam changePlayer() {
            return addOrRemovePlayer(3, this.currentPlayer);
        }

        @SuppressWarnings("unchecked")
        PacketPlayOutScoreboardTeam addOrRemovePlayer(int mode, String playerName) {
            PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
            setField(packet, "a", this.name);
            setField(packet, "h", mode);

            try {
                Field f = packet.getClass().getDeclaredField("g");
                f.setAccessible(true);
                ((List<String>) f.get(packet)).add(playerName);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            return packet;
        }

        String getCurrentPlayer() {
            return this.currentPlayer;
        }

        String getValue() {
            return getPrefix() + getCurrentPlayer() + getSuffix();
        }

        void setValue(String value) {
            if (value.length() <= 16) {
                setPrefix(value);
                setSuffix("");
            } else if (value.length() <= 32) {
                String first = value.substring(0, 16);
                String second = value.substring(16);
                if (first.endsWith("§")) {
                    first = first.substring(0, 15);
                    second = "§" + second;
                }
                if (second.length() > 16)
                    second = second.substring(16, 32);
                setPrefix(first);
                setSuffix(second);
            } else {
                throw new IllegalArgumentException(
                        "Too long value ! Max 32 characters, value was " + value.length() + " !");
            }
        }
    }

}
