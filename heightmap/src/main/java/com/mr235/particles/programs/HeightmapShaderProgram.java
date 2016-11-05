package com.mr235.particles.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.mr235.particles.R;

/**
 * Created by Administrator on 2016/11/5.
 */

public class HeightmapShaderProgram extends ShaderProgram {

    // Uniform locations
    private final int uMatrixLocation;

    // Attribute locations
    private final int aPositionLocation;

    public HeightmapShaderProgram(Context context) {
        super(context, R.raw.heightmap_vertex_shader, R.raw.heightmap_fragment_shader);

        // Retrieve uniform locations for the shader program.
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX);

        // Retrieve attribute locations for the shader program.
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
    }

    public void setUniforms(float[] matrix) {
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, matrix, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

}
