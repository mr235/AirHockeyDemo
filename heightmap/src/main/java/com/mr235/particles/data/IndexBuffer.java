package com.mr235.particles.data;

import android.opengl.GLES20;

import com.mr235.particles.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Created by Administrator on 2016/11/5.
 */

public class IndexBuffer {
    private final int bufferId;

    public IndexBuffer(short[] vertexData) {
        // Allocate a buffer.
        final int[] buffers = new int[1];
        GLES20.glGenBuffers(buffers.length, buffers, 0);
        if (buffers[0] == 0) {
            throw new RuntimeException("Could not create a new vertex buffer object.");
        }
        bufferId = buffers[0];

        // Bind to the buffer.
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, buffers[0]);

        // Transfer data to native memory.
        ShortBuffer vertexArray = ByteBuffer
                .allocateDirect(vertexData.length * Constants.BYTES_PER_SHORT)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(vertexData);

        vertexArray.position(0);

        // Transfer data from native memory to the GPU buffer.
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, vertexArray.capacity() * Constants.BYTES_PER_SHORT, vertexArray, GLES20.GL_STATIC_DRAW);

        // IMPORTANT: Unbind from the buffer when we're done with it.
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public int getBufferId() {
        return bufferId;
    }
}
