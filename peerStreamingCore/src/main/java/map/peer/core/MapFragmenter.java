package map.peer.core;

import java.util.Arrays;

import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.Fragmenter;
import com.googlecode.mp4parser.util.Mp4Arrays;

public class MapFragmenter implements Fragmenter {
    private double fragmentLength = 2.0D;

    public MapFragmenter(double fragmentLength) {
        this.fragmentLength = fragmentLength;
    }

    public long[] sampleNumbers(Track track) {
        long[] segmentStartSamples = new long[]{1L};
        long[] sampleDurations = track.getSampleDurations();
        long[] syncSamples = track.getSyncSamples();
        long timescale = track.getTrackMetaData().getTimescale();
        double time = 0.0D;

        for (int i = 0; i < sampleDurations.length; ++i) {
            time += (double) sampleDurations[i] / (double) timescale;
            if (time >= this.fragmentLength && (syncSamples == null || Arrays.binarySearch(syncSamples, (long) (i + 1)) >= 0)) {
                if (i > 0) {
                    segmentStartSamples = Mp4Arrays.copyOfAndAppend(segmentStartSamples, (long) (i + 1));
                }

                time = 0.0D;
            }
        }
        // In case the last Fragment is shorter: make the previous one a bigger and omit the small one
        if (time > 0) {
            long[] nuSegmentStartSamples = new long[segmentStartSamples.length - 1];
            System.arraycopy(segmentStartSamples, 0, nuSegmentStartSamples, 0, segmentStartSamples.length - 1);
            segmentStartSamples = nuSegmentStartSamples;
        }

        return segmentStartSamples;
    }


}
