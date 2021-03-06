package com.mr235.airhockey.objects;

import com.mr235.airhockey.data.VertexArray;
import com.mr235.airhockey.programs.ColorShaderProgram;
import com.mr235.airhockey.util.Geometry;

import java.util.List;

/**
 * Created by Administrator on 2016/10/8.
 */

public class Mallet {
    private static final int POSITION_COMPONENT_COUNT = 3;

    public final float radius;
    public final float height;

    private final VertexArray vertexArray;
    private final List<ObjectBuilder.DrawCommand> drawList;

    public Mallet(float radius, float height, int numPointsAroundPuck) {
        ObjectBuilder.GeneratedData generatedData = ObjectBuilder.createMallet(
                new Geometry.Point(0f, 0f, 0f), radius, height,
                numPointsAroundPuck);

        this.radius = radius;
        this.height = height;

        vertexArray = new VertexArray(generatedData.vertexData);
        drawList = generatedData.drawList;
    }

    public void bindData(ColorShaderProgram colorProgram) {
        vertexArray.setVertexAttribPointer(0, colorProgram.getPositionAttributeLocation(),
                POSITION_COMPONENT_COUNT, 0);
    }

    public void draw() {
        for (ObjectBuilder.DrawCommand drawCommand : drawList) {
            drawCommand.draw();
        }
    }
}
