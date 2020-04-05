package ovh.roro.pitchout.util;

/**
 * @author roro1506_HD
 */
public class ScoreboardTextAnimator {

    private String text;
    private String actualText;
    private int index;
    private String baseColor;
    private String toColorText;
    private String centerColor;

    public ScoreboardTextAnimator(String text, String baseColor, String toColorText, String centerColor) {
        this.text = text;
        this.index = -20;
        this.baseColor = baseColor;
        this.toColorText = toColorText;
        this.centerColor = centerColor;
        this.actualText = baseColor + text;
    }

    public int getIndex() {
        return this.index;
    }

    public String getActualText() {
        return this.actualText;
    }

    public boolean next() {
        String oldText = this.actualText;
        if (this.index - 20 == this.text.length()) {
            this.index = -20;
            this.actualText = this.baseColor + this.text;
            return (!oldText.equals(this.actualText));
        } else {
            StringBuilder finalText = new StringBuilder(this.baseColor);
            int i = 0;
            char[] chars;
            int var5 = (chars = this.text.toCharArray()).length;

            for (int j = 0; j < var5; ++j) {
                char c = chars[j];
                if (i == this.index)
                    finalText.append(this.centerColor).append(c);
                else if (i - 1 != this.index && i + 1 != this.index)
                    finalText.append(c);
                else
                    finalText.append(this.toColorText).append(c).append(this.baseColor);

                ++i;
            }

            ++this.index;
            this.actualText = finalText.toString();
            return (!oldText.equals(this.actualText));
        }
    }
}
