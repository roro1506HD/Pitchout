package ovh.roro.pitchout.util.item.modifier;

import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import ovh.roro.pitchout.util.item.IItemModifier;
import ovh.roro.pitchout.util.item.ItemBuilder;

/**
 * @author roro1506_HD
 */
public class BookModifier implements IItemModifier {

    private final ItemBuilder builder;

    private final NBTTagList pages;
    private NBTTagString author;
    private NBTTagString title;

    public BookModifier(ItemBuilder builder) {
        this.builder = builder;

        this.pages = builder.getTag().getList("pages", 8);
        this.author = new NBTTagString(builder.getTag().getString("author"));
        this.title = new NBTTagString(builder.getTag().getString("title"));
    }

    public BookModifier addPage(String page) {
        this.pages.add(new NBTTagString(page));
        return this;
    }

    public BookModifier setTitle(String title) {
        this.title = new NBTTagString(title);
        return this;
    }

    public BookModifier setAuthor(String author) {
        this.author = new NBTTagString(author);
        return this;
    }

    @Override
    public ItemBuilder apply() {
        return this.builder
                .addNBTTag("pages", this.pages)
                .addNBTTag("author", this.author)
                .addNBTTag("title", this.title)
                .addNBTTag("resolved", new NBTTagByte((byte) 1));
    }
}
