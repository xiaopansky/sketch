package me.panpf.sketch.decode;

import androidx.annotation.NonNull;

import me.panpf.sketch.SketchException;
import me.panpf.sketch.process.ImageProcessor;

public class ProcessImageException extends SketchException {
    @NonNull
    private String imageUri;
    @NonNull
    private ImageProcessor processor;

    public ProcessImageException(@NonNull Throwable cause, @NonNull String imageUri, @NonNull ImageProcessor processor) {
        super(cause);
        this.imageUri = imageUri;
        this.processor = processor;
    }

    @NonNull
    @Override
    public synchronized Throwable getCause() {
        return super.getCause();
    }

    @NonNull
    public String getImageUri() {
        return imageUri;
    }

    @NonNull
    public ImageProcessor getProcessor() {
        return processor;
    }
}
