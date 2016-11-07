package com.mr235.particles.programs;

import android.content.Context;
import android.opengl.GLES20;

import com.mr235.particles.R;

/**
 * Created by Administrator on 2016/11/5.
 */

public class HeightmapShaderProgram extends ShaderProgram {

    // Uniform locations
    private final int uVectorToLightLocation;
    private int uMVMatrixLocation;
    private int uIT_MVMatrixLocation;
    private int uMVPMatrixLocation;
    private int uPointLightPositionsLocation;
    private int uPointLightColorsLocation;

    // Attribute locations
    private final int aPositionLocation;
    private final int aNormalLocation;

    public HeightmapShaderProgram(Context context) {
        super(context, R.raw.heightmap_vertex_shader, R.raw.heightmap_fragment_shader);

        // Retrieve uniform locations for the shader program.
        uVectorToLightLocation = GLES20.glGetUniformLocation(program, U_VECTOR_TO_LIGHT);
        uMVMatrixLocation = GLES20.glGetUniformLocation(program, U_MV_MATRIX);
        uIT_MVMatrixLocation = GLES20.glGetUniformLocation(program, U_IT_MV_MATRIX);
        uMVPMatrixLocation = GLES20.glGetUniformLocation(program, U_MVP_MATRIX);

        uPointLightPositionsLocation = GLES20.glGetUniformLocation(program, U_POINT_LIGHT_POSITIONS);
        uPointLightColorsLocation = GLES20.glGetUniformLocation(program, U_POINT_LIGHT_COLORS);

        // Retrieve attribute locations for the shader program.
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        aNormalLocation = GLES20.glGetAttribLocation(program, A_NORMAL);
    }

    public void setUniforms(float[] mvMatrix,
                            float[] it_mvMatrix,
                            float[] mvpMatrix,
                            float[] vectorToDirectionalLight,
                            float[] pointLightPositions,
                            float[] pointLightColors) {
        GLES20.glUniformMatrix4fv(uMVMatrixLocation, 1, false, mvMatrix, 0);
        GLES20.glUniformMatrix4fv(uIT_MVMatrixLocation, 1, false, it_mvMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixLocation, 1, false, mvpMatrix, 0);

        GLES20.glUniform3fv(uVectorToLightLocation, 1, vectorToDirectionalLight, 0);
        GLES20.glUniform4fv(uPointLightPositionsLocation, 3, pointLightPositions, 0);
        GLES20.glUniform3fv(uPointLightColorsLocation, 3, pointLightColors, 0);
    }

    public int getPositionAttributeLocation() {
        return aPositionLocation;
    }

    public int getNormalAttributeLocation() {
        return aNormalLocation;
    }
}
