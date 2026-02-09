package app.hub.onboarding;

import androidx.annotation.DrawableRes;
import java.util.Objects;

/**
 * Data class representing a single onboarding screen configuration.
 * 
 * This class encapsulates all the information needed to display one onboarding screen,
 * including the background image, title, and subtitle text.
 */
public class OnboardingPage {
    @DrawableRes
    private final int backgroundImageRes;
    private final String titleText;
    private final String subtitleText;

    /**
     * Creates a new OnboardingPage with the specified configuration.
     *
     * @param backgroundImageRes Drawable resource ID for the background image
     * @param titleText Main title text displayed on the screen
     * @param subtitleText Subtitle text displayed below the title
     * @throws IllegalArgumentException if backgroundImageRes is 0, or if titleText or subtitleText is blank
     */
    public OnboardingPage(@DrawableRes int backgroundImageRes, String titleText, String subtitleText) {
        if (backgroundImageRes == 0) {
            throw new IllegalArgumentException("backgroundImageRes must be a valid drawable resource ID (non-zero)");
        }
        if (titleText == null || titleText.trim().isEmpty()) {
            throw new IllegalArgumentException("titleText must not be empty or blank");
        }
        if (subtitleText == null || subtitleText.trim().isEmpty()) {
            throw new IllegalArgumentException("subtitleText must not be empty or blank");
        }

        this.backgroundImageRes = backgroundImageRes;
        this.titleText = titleText;
        this.subtitleText = subtitleText;
    }

    /**
     * @return The drawable resource ID for the background image
     */
    @DrawableRes
    public int getBackgroundImageRes() {
        return backgroundImageRes;
    }

    /**
     * @return The main title text
     */
    public String getTitleText() {
        return titleText;
    }

    /**
     * @return The subtitle text
     */
    public String getSubtitleText() {
        return subtitleText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnboardingPage that = (OnboardingPage) o;
        return backgroundImageRes == that.backgroundImageRes &&
                Objects.equals(titleText, that.titleText) &&
                Objects.equals(subtitleText, that.subtitleText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backgroundImageRes, titleText, subtitleText);
    }

    @Override
    public String toString() {
        return "OnboardingPage{" +
                "backgroundImageRes=" + backgroundImageRes +
                ", titleText='" + titleText + '\'' +
                ", subtitleText='" + subtitleText + '\'' +
                '}';
    }
}
