package com.assignment.main;

public class Articles {
	private String spanishTitle;
    private String spanishContent;
    private String imageUrl;
    private String translatedTitle;

    public Articles(String spanishTitle, String spanishContent, String imageUrl) {
        this.spanishTitle = spanishTitle;
        this.spanishContent = spanishContent;
        this.imageUrl = imageUrl;
    }

    public String getSpanishTitle() {
        return spanishTitle;
    }

    public String getSpanishContent() {
        return spanishContent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTranslatedTitle() {
        return translatedTitle;
    }

    public void setTranslatedTitle(String translatedTitle) {
        this.translatedTitle = translatedTitle;
    }

}
