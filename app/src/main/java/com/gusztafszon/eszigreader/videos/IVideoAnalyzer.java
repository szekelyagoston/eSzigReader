package com.gusztafszon.eszigreader.videos;

import java.util.List;

/**
 * Created by Gusztafszon on 2017-03-20.
 */

public interface IVideoAnalyzer {
    void addFrame(VideoFrame videoFrame);

    List<VideoFrame> getFrames();

    List<VideoFrame> filterFrames();

    void resetFrames();
}
