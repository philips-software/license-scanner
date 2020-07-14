package com.philips.research.licensescanner.core.domain.download;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

public class HttpHandler implements VcsHandler {
    @Override
    public void download(Path directory, VcsUri location) {
        try {
            //TODO Prototype code
            final var url = location.getRepository().toURL();
            ReadableByteChannel inChannel = Channels.newChannel(url.openStream());
            FileOutputStream out = new FileOutputStream("filename");
            FileChannel outChannel = out.getChannel();
            outChannel.transferFrom(inChannel, 0, Long.MAX_VALUE);
            // Need to close streams!
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        }
    }
}
