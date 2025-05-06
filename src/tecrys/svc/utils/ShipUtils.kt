package tecrys.svc.utils

import com.fs.starfarer.api.combat.ShipAPI
import org.lwjgl.util.vector.Vector2f
import java.util.ArrayList

data class ShipDimensionsResult(
    val minX: Float, val minY: Float,
    val maxX: Float,
    val maxY: Float,
    val polygon: ArrayList<Vector2f>
)

object ShipUtils {
    fun getShipDimensions(ship: ShipAPI): ShipDimensionsResult {
        val max = Vector2f(0f, 0f)
        val min = Vector2f(10000f, 10000f)
        val polygon = ArrayList<Vector2f>()

        ship.exactBounds.origSegments.forEach { segment ->
            polygon.add(segment.p1)

            // max
            if (segment.p1.x > max.x) {
                max.x = segment.p1.x
            }

            if (segment.p1.y > max.y) {
                max.y = segment.p1.y
            }

            // min
            if (segment.p1.x < min.x) {
                min.x = segment.p1.x
            }

            if (segment.p1.y < min.y) {
                min.y = segment.p1.y
            }
        }

        return ShipDimensionsResult(min.x, min.y, max.x, max.y, polygon)
    }

    /**
     * Generates random points inside a polygon (works with concave shapes)
     * @param polygon Array of Vector2f points defining the polygon vertices
     * @param pointCount Number of points to generate (default 2)
     * @return Array of Vector2f containing the generated points
     */
    fun getRandomPointsInShip(ship: ShipAPI, pointCount: Int = 2): ArrayList<Vector2f> {

        val dimensions = getShipDimensions(ship)

        // Find bounding box
        val results = ArrayList<Vector2f>();

        while (results.size < pointCount) {
            // Generate random point within bounding box
            val x = dimensions.minX + (dimensions.maxX - dimensions.minX) * Math.random().toFloat()
            val y = dimensions.minY + (dimensions.maxY - dimensions.minY) * Math.random().toFloat()
            val point = Vector2f(x, y)

            // Check if point is inside polygon using ray casting algorithm
            if (isPointInPolygon(point, dimensions.polygon)) {
                results.add(point)
            }
        }

        return results
    }

    /**
     * Checks if a point is inside a polygon using ray casting algorithm
     * @param point The point to check
     * @param polygon Array of Vector2f defining the polygon vertices
     * @return true if point is inside the polygon
     */
    private fun isPointInPolygon(point: Vector2f, polygon: ArrayList<Vector2f>): Boolean {
        var inside = false
        var j = polygon.size - 1

        for (i in 0..polygon.size) {
            if (i == polygon.size){
                continue
            }

            if ((polygon[i].y > point.y) != (polygon[j].y > point.y) &&
                point.x < (polygon[j].x - polygon[i].x) * (point.y - polygon[i].y) /
                (polygon[j].y - polygon[i].y) + polygon[i].x
            ) {
                inside = !inside
            }
            j = i
        }

        return inside
    }
}