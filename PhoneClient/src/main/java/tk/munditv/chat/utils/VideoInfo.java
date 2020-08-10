package tk.munditv.chat.utils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class VideoInfo {
    @NonNull private final String videoId;
    @NonNull private final String videoTitle;
    @NonNull private final String channelTitle;
    @NonNull private final String description;
    @NonNull private final String thumbnailURL;
    @NonNull private final Bitmap thumbnail;

    VideoInfo(@NonNull String videoId, @NonNull String videoTitle, @NonNull String channelTitle,
              @Nullable String description, @NonNull String thumbnailURL, @NonNull Bitmap thumbnail) {
        this.videoId = videoId;
        this.videoTitle = videoTitle;
        this.channelTitle = channelTitle;
        this.description = description;
        this.thumbnailURL = thumbnailURL;
        this.thumbnail = thumbnail;
    }

    @NonNull
    public String getVideoId() {
        return videoId;
    }

    @NonNull
    public String getVideoTitle() {
        return videoTitle;
    }

    @NonNull
    public String getChannelTitle() {
        return channelTitle;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    @NonNull
    public String getThumbnailURL() {
        return thumbnailURL;
    }

    @NonNull
    public Bitmap getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoInfo videoInfo = (VideoInfo) o;

        if (!videoTitle.equals(videoInfo.videoTitle)) return false;
        if (!channelTitle.equals(videoInfo.channelTitle)) return false;
        return thumbnail.equals(videoInfo.thumbnail);
    }

    @Override
    public int hashCode() {
        int result = videoTitle.hashCode();
        result = 31 * result + channelTitle.hashCode();
        result = 31 * result + thumbnail.hashCode();
        return result;
    }
}
